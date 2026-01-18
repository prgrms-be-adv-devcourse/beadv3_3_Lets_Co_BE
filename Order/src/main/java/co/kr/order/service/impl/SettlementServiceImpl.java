package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    .type(SettlementType.SALE)
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
                    .type(SettlementType.REFUND)
                    .paymentIdx(paymentIdx)
                    .amount(entry.getValue())
                    .build();

            settlementRepository.save(settlement);
            log.info("환불 정산 생성 완료: sellerIdx={}, amount={}, paymentIdx={}",
                    entry.getKey(), entry.getValue(), paymentIdx);
        }
    }
}
