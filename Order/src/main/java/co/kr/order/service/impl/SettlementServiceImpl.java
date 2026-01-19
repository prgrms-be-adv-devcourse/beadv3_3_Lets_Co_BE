package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.model.dto.SellerInfo;
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
    private final UserClient userClient;

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
                    .type(SettlementType.ORDERS_CONFIRMED)
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
     * 4. 판매자별로 환불 금액 합산
     * 5. 판매자별 REFUND Settlement 레코드 생성
     */
    @Override
    @Transactional
    public void createRefundSettlement(Long orderId, Long paymentIdx) {
        // 주문 조회
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId=" + orderId));

        // OrderItem에서 상품 ID 목록 추출
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

        //  판매자별로 환불 금액 합산
        Map<Long, BigDecimal> sellerAmountMap = new HashMap<>();
        for (OrderItemEntity item : orderItems) {
            Long sellerIdx = productSellerMap.get(item.getProductIdx());
            if (sellerIdx == null) {
                log.warn("판매자 정보를 찾을 수 없습니다. productIdx={}", item.getProductIdx());
                continue;
            }

            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            sellerAmountMap.merge(sellerIdx, itemTotal, BigDecimal::add);
        }

        // 판매자별 레코드 생성
        for (Map.Entry<Long, BigDecimal> entry : sellerAmountMap.entrySet()) {
            SettlementHistoryEntity settlement = SettlementHistoryEntity.builder()
                    .sellerIdx(entry.getKey())
                    .type(SettlementType.SETTLE_PAYOUT)
                    .paymentIdx(paymentIdx)
                    .amount(entry.getValue())
                    .build();

            settlementRepository.save(settlement);
            log.info("환불 정산 생성 완료: sellerIdx={}, amount={}, paymentIdx={}",
                    entry.getKey(), entry.getValue(), paymentIdx);
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

    // 정산처리
    @Transactional
    public String processSettlement() {

        List<SettlementHistoryEntity> entities = settlementRepository.findAll();
        if(entities.isEmpty()) return null;

        Map<Long, BigDecimal> sellerResponse = new HashMap<>();

        for(SettlementHistoryEntity entity : entities) {

            if (entity.getType() == SettlementType.SETTLE_PAYOUT || entity.getType() == SettlementType.CANCEL_ADJUST) {
                continue;
            }

            Long sellerIdx = entity.getSellerIdx();
            BigDecimal amount = entity.getAmount();

            entity.setType(SettlementType.CANCEL_ADJUST);

            sellerResponse.merge(sellerIdx, amount, BigDecimal::add);
        }

        Set<Long> sellerIds = sellerResponse.keySet();
        if (!sellerResponse.isEmpty()) {

            // 만약 정산금을 예치금으로 준다면
            if(true) {
                userClient.sendSettlementData(sellerResponse);
                return "정산금 처리 완료(예치금)" ;
            }

            // 만약 정산금을 통장으로 준다면
            if(true) {
                SellerInfo sellerInfo = userClient.getSellerData(sellerIds);

                Long sellerIdx = sellerInfo.sellerIdx();
                String businessLicense = sellerInfo.businessLicense();
                String bankBrand = sellerInfo.bankBrand();
                String bankName = sellerInfo.bankName();
                String bankToken = sellerInfo.bankToken();

                // sellerInfo의 데이터 가지고 계좌에 돈을 주는 로직 (이건 구현 못함)
                return "정산금 처리 완료(카드)";
            }
        }

        return null;
    }

}
