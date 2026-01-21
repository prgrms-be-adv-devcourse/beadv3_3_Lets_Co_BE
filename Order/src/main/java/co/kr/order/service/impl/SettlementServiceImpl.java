package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.model.dto.SettlementInfo;
import co.kr.order.model.entity.OrderEntity;
import co.kr.order.model.entity.OrderItemEntity;
import co.kr.order.model.entity.PaymentEntity;
import co.kr.order.model.entity.SettlementHistoryEntity;
import co.kr.order.model.vo.SettlementType;
import co.kr.order.repository.OrderJpaRepository;
import co.kr.order.repository.PaymentJpaRepository;
import co.kr.order.repository.SettlementRepository;
import co.kr.order.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final OrderJpaRepository orderRepository;
    private final PaymentJpaRepository paymentRepository;
    private final ProductClient productClient;

    /**
     * 정산 생성 (주문 완료 시 호출)
     *
     * 처리 흐름:
     * 1. 주문 정보 조회
     * 2. 해당 주문의 결제 정보 조회
     * 3. OrderItem에서 상품 ID 목록 추출
     * 4. Product 서비스에서 상품별 판매자 정보 조회
     * 5. 판매자별로 금액 합산
     * 6. 판매자별 Settlement 레코드 생성
     */
    @Override
    @Transactional
    public void createSettlement(Long orderId) {

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId=" + orderId));

        PaymentEntity payment = paymentRepository.findByOrdersIdx(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 내역을 찾을 수 없습니다. orderId=" + orderId));

        List<OrderItemEntity> orderItems = order.getOrderItems();
        if (orderItems.isEmpty()) {
            log.warn("주문 상품이 없습니다. orderId={}", orderId);
            return;
        }

        List<Long> productIds = orderItems.stream()
                .map(OrderItemEntity::getProductIdx)
                .toList();

        // Product 서비스에서 상품별 판매자 정보 조회
        Map<Long, Long> productSellerMap = productClient.getSellersByProductIds(productIds);

        Map<Long, BigDecimal> sellerAmountMap = new HashMap<>();
        for (OrderItemEntity item : orderItems) {
            Long sellerIdx = productSellerMap.get(item.getProductIdx());
            if (sellerIdx == null) {
                log.warn("판매자 정보를 찾을 수 없습니다. productIdx={}", item.getProductIdx());
                continue;
            }

            // 상품 가격 * 수량 = 상품별 금액
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            sellerAmountMap.merge(sellerIdx, itemTotal, BigDecimal::add);
        }

        for (Map.Entry<Long, BigDecimal> entry : sellerAmountMap.entrySet()) {
            SettlementHistoryEntity settlement = SettlementHistoryEntity.builder()
                    .sellerIdx(entry.getKey())
                    .type(SettlementType.Orders_CONFIRMED)
                    .paymentIdx(payment.getPaymentIdx())
                    .amount(entry.getValue())
                    .build();

            settlementRepository.save(settlement);
            log.info("정산 생성 완료: sellerIdx={}, amount={}, paymentIdx={}",
                    entry.getKey(), entry.getValue(), payment.getPaymentIdx());
        }
    }

    /**
     * 환불 정산 생성 (환불 시 호출)
     *
     * 처리 흐름:
     * 1. 주문 정보 조회
     * 2. OrderItem에서 상품 ID 목록 추출
     * 3. Product 서비스에서 상품별 판매자 정보 조회
     * 4. 판매자별 기존 정산 레코드(Orders_CONFIRMED) 조회
     * 5. 급 완료가 아니면 CANCEL_ADJUST로 상태 변경
     */
    @Override
    @Transactional
    public void refundSettlement(Long orderId, Long paymentIdx) {

        // 주문 조회
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId=" + orderId));

        List<OrderItemEntity> orderItems = order.getOrderItems();
        if (orderItems.isEmpty()) {
            log.warn("주문 상품이 없습니다. orderId={}", orderId);
            return;
        }

        List<Long> productIds = orderItems.stream()
                .map(OrderItemEntity::getProductIdx)
                .toList();

        // Product 서비스에서 상품별 판매자 정보 조회
        Map<Long, Long> productSellerMap = productClient.getSellersByProductIds(productIds);

        Set<Long> sellerIds = new HashSet<>(productSellerMap.values());

        for (Long sellerIdx : sellerIds) {
            SettlementHistoryEntity settlementEntity = settlementRepository.findBySellerIdxAndPaymentIdx(sellerIdx, paymentIdx);

            if (settlementEntity != null) {
                // SETTLE_PAYOUT 환불 시 에러 로그 및 수동 처리 필요
                if (settlementEntity.getType() == SettlementType.SETTLE_PAYOUT) {
                    log.error("========================================");
                    log.error("에러: PAYOUT 건에 대한 환불 요청 발생!");
                    log.error("확인 필요 - sellerIdx: {}, paymentIdx: {}, amount: {}",
                            sellerIdx, paymentIdx, settlementEntity.getAmount());
                    log.error("========================================");
                    // 상태 변경하지 않음 (수동 관리 프로세스로 넘김)
                    continue;
                }

                settlementEntity.setType(SettlementType.CANCEL_ADJUST);

                log.info("환불 정산 상태 변경 완료: sellerIdx={}, paymentIdx={}", sellerIdx, paymentIdx);
            } else {
                log.error("정산 데이터를 찾을 수 없습니다! sellerIdx={}, paymentIdx={}", sellerIdx, paymentIdx);
            }
        }
    }

    /*
     * 정산목록 조회를 Order-service 쪽에서 할지, Member-service에서 할지?
     * member에서 sellerIdx 주면 Order에서 sellerIdx 맞는거 찾아 List나 단건을 주기만 함면 될거같음
     */
    @Transactional
    @Override
    public List<SettlementInfo> getSettlementList(Long sellerIdx) {

        List<SettlementHistoryEntity> entities = settlementRepository.findAllBySellerIdx(sellerIdx);

        List<SettlementInfo> returnSettlementList = new ArrayList<>();

        for(SettlementHistoryEntity entity : entities) {
            SettlementInfo settlementInfo = new SettlementInfo(
                    entity.getSettlementIdx(),
                    entity.getSellerIdx(),
                    entity.getPaymentIdx(),
                    entity.getType().toString(),
                    entity.getAmount(),
                    entity.getCreatedAt()
            );

            returnSettlementList.add(settlementInfo);
        }

        return returnSettlementList;
    }

    @Transactional
    @Override
    public SettlementInfo getSettlement(Long sellerIdx, Long paymentIdx) {

        SettlementHistoryEntity entity = settlementRepository.findBySellerIdxAndPaymentIdx(sellerIdx, paymentIdx);

        return new SettlementInfo(
                entity.getSettlementIdx(),
                entity.getSellerIdx(),
                entity.getPaymentIdx(),
                entity.getType().toString(),
                entity.getAmount(),
                entity.getCreatedAt()
        );
    }
}
