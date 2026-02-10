package co.kr.order.service.impl;

import co.kr.order.client.PaymentClient;
import co.kr.order.client.ProductClient;
import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.OrderNotFoundException;
import co.kr.order.model.dto.ItemInfo;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.request.ClientPaymentReq;
import co.kr.order.model.dto.request.ClientProductReq;
import co.kr.order.model.dto.request.ClientRefundReq;
import co.kr.order.model.dto.request.OrderReq;
import co.kr.order.model.dto.response.ClientPaymentRes;
import co.kr.order.model.dto.response.ClientProductRes;
import co.kr.order.model.dto.response.OrderItemRes;
import co.kr.order.model.dto.response.OrderRes;
import co.kr.order.model.entity.OrderEntity;
import co.kr.order.model.entity.OrderItemEntity;
import co.kr.order.model.vo.OrderStatus;
import co.kr.order.model.vo.OrderType;
import co.kr.order.repository.OrderItemJpaRepository;
import co.kr.order.repository.OrderJpaRepository;
import co.kr.order.service.CartService;
import co.kr.order.service.DeductStockService;
import co.kr.order.service.OrderService;
import co.kr.order.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;

    private final CartService cartService;
    private final DeductStockService deductStockService;
    private final SettlementService settlementService;

    private final PaymentClient paymentClient;
    private final ProductClient productClient;

    @Transactional
    @Override
    public OrderRes createOrder(Long userIdx, OrderReq request) {

        String orderCode = UUID.randomUUID().toString();

        List<OrderItemEntity> tempOrderItems = new ArrayList<>(); // 임시 주문 아이템
        List<ProductInfo> stocksInfos = new ArrayList<>();  // 재고 정보
        Map<Long, BigDecimal> sellerSettlementMap = new HashMap<>();  // 판매자별 정산금


        // =================================================================
        // 주문 상품 데이터 준비
        // =================================================================
        switch (request.orderType()) {
            case OrderType.DIRECT:
                ClientProductRes product = productClient.getProduct(
                        request.productInfo().productCode(),
                        request.productInfo().optionCode()
                );

                tempOrderItems.add(createOrderItemEntity(product, request.productInfo().quantity()));

                // 재고 차감할 데이터 세팅
                stocksInfos.add(
                        new ProductInfo(
                            request.productInfo().productCode(),
                            request.productInfo().optionCode(),
                            request.productInfo().quantity()
                        )
                );

                // 정산 데이터 수집
                BigDecimal directAmount = product.price().multiply(BigDecimal.valueOf(request.productInfo().quantity()));
                sellerSettlementMap.merge(product.sellerIdx(), directAmount, BigDecimal::add);

                break;

            case OrderType.CART:
                Map<Long, Integer> quantityMap = cartService.getCartItemQuantities(userIdx);
                List<ClientProductReq> productRequest = cartService.getProductByCart(userIdx);
                List<ClientProductRes> productsResponse = productClient.getProductList(productRequest);

                for (ClientProductRes productRes : productsResponse) {
                    Integer quantity = quantityMap.getOrDefault(productRes.optionIdx(), 0);
                    if (quantity > 0) {
                        tempOrderItems.add(createOrderItemEntity(productRes, quantity));

                        // 재고 차감할 데이터 세팅
                        stocksInfos.add(
                                new ProductInfo(
                                        productRes.productCode(),
                                        productRes.optionCode(),
                                        quantity
                                )
                        );

                        // 정산 데이터 수집
                        BigDecimal itemAmount = productRes.price().multiply(BigDecimal.valueOf(quantity));
                        sellerSettlementMap.merge(productRes.sellerIdx(), itemAmount, BigDecimal::add);
                    }
                }

                break;

            default:
                throw new RuntimeException("올바르지 않는 주문타입");
        }

        // Redis 재고 선차감 (Atomic)
        deductStockService.decreaseStocks(stocksInfos);

        BigDecimal totalAmount = tempOrderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderEntity orderEntity = OrderEntity.builder()
                .userIdx(userIdx)
                .orderCode(orderCode)
                .recipient(request.addressInfo().recipient())
                .address(request.addressInfo().address())
                .addressDetail(request.addressInfo().addressDetail())
                .phone(request.addressInfo().phone())
                .itemsAmount(totalAmount)
                .totalAmount(totalAmount)
                .build();

        try {
            orderRepository.save(orderEntity);

            for (OrderItemEntity item : tempOrderItems) {
                item.setOrder(orderEntity);
            }
            orderItemRepository.saveAll(tempOrderItems);
        } catch (Exception e) {
            log.error("주문 DB 저장 실패. 재고 롤백 수행. orderCode={}", orderCode);
            deductStockService.rollBackStocks(stocksInfos);
            throw e; // 예외 다시 던져서 트랜잭션 롤백
        }


        // =================================================================
        // 결제 요청 -> 5. 재고 차감/정산 -> (실패시) 환불
        // =================================================================
        ClientPaymentRes paymentRes;
        try {
            paymentRes = paymentClient.processPayment(
                    new ClientPaymentReq(
                            userIdx,
                            orderCode,
                            orderEntity.getId(),
                            request.paymentType(),
                            totalAmount,
                            request.tossKey()
                    )
            );
            orderEntity.setStatus(OrderStatus.PAID);

        } catch (Exception e) {
            log.error("결제 실패. 재고 롤백 수행. orderCode={}", orderCode);
            deductStockService.rollBackStocks(stocksInfos);

            // 트랜잭션이 알아서 RollBack 해줌 (명시용)
            orderEntity.setStatus(OrderStatus.REFUNDED);

            throw new RuntimeException("결제 실패", e);
        }

        try {
            // 정산 데이터 저장
            settlementService.createSettlement(paymentRes.paymentIdx(), sellerSettlementMap);

            // 장바구니 비우기
            if (request.orderType() == OrderType.CART) {
                cartService.deleteCartAll(userIdx);
            }

            /*
             * ===========================================
             * todo. kafka로 재고를 갱신하라는 메세지 발행
             * ===========================================
             */

        } catch (Exception e) {
            log.error("주문 후처리 실패. 결제 취소 및 재고 롤백 수행. orderCode={}", orderCode, e);
            deductStockService.rollBackStocks(stocksInfos);

            try {
                paymentClient.refundPayment(new ClientRefundReq(userIdx, orderCode));
            } catch (Exception refundEx) {
                log.error("CRITICAL: 결제 취소(환불) 요청 실패! 관리자 개입 필요. orderCode={}", orderCode, refundEx);
            }

            orderEntity.setStatus(OrderStatus.REFUNDED);
            throw new RuntimeException("주문 마무리 중 오류 발생 (환불 완료)", e);
        }

        List<OrderItemRes> responseItems = tempOrderItems.stream()
                .map(item -> new OrderItemRes(
                        new ItemInfo(
                                item.getProductCode(),
                                item.getOptionCode(),
                                item.getProductName(),
                                item.getOptionName(),
                                item.getPrice()),
                        item.getQuantity(),
                        item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .collect(Collectors.toList());

        return new OrderRes(orderCode, responseItems, totalAmount);
    }

    private OrderItemEntity createOrderItemEntity(ClientProductRes product, Integer quantity) {
        return OrderItemEntity.builder()
                .productCode(product.productCode())
                .optionCode(product.optionCode())
                .productName(product.productName())
                .optionName(product.optionContent())
                .price(product.price())
                .quantity(quantity)
                .del(false)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<OrderRes> findOrderList(Long userIdx, Pageable pageable) {

        Page<OrderEntity> orderPage = orderRepository.findAllByUserIdx(userIdx, pageable);

        return orderPage.map(orderEntity -> {

            List<OrderItemRes> responseItemList = new ArrayList<>();
            BigDecimal itemsAmount = BigDecimal.ZERO;

            List<OrderItemEntity> itemEntities = orderEntity.getOrderItems();
            for (OrderItemEntity itemEntity : itemEntities) {

                BigDecimal amount = itemEntity.getPrice().multiply(BigDecimal.valueOf(itemEntity.getQuantity()));
                itemsAmount = itemsAmount.add(amount);

                responseItemList.add(
                        new OrderItemRes(
                                new ItemInfo(
                                        itemEntity.getProductCode(),
                                        itemEntity.getOptionCode(),
                                        itemEntity.getProductName(),
                                        itemEntity.getOptionName(),
                                        itemEntity.getPrice()
                                ),
                                itemEntity.getQuantity(),
                                amount
                        )
                );
            }

            return new OrderRes(
                    orderEntity.getOrderCode(),
                    responseItemList,
                    itemsAmount
            );
        });
    }

    /*  [이전 N+1 발생 코드]
    List<OrderEntity> orderEntities = orderRepository.findAllByUserIdx(userIdx);
    for(OrderEntity orderEntity : orderEntities) {

        List<OrderItemResponse> responseItemList = new ArrayList<>();

        BigDecimal itemsAmount = BigDecimal.ZERO;
        List<OrderItemEntity> itemEntities = orderItemRepository.findAllByOrder(orderEntity);
        for(OrderItemEntity itemEntity : itemEntities) {

            BigDecimal amount = itemEntity.getPrice().multiply(BigDecimal.valueOf(itemEntity.getQuantity()));
            itemsAmount = itemsAmount.add(amount);
            responseItemList.add(
                    new OrderItemResponse(
                            new ItemInfo(
                                    itemEntity.getProductIdx(),
                                    itemEntity.getOptionIdx(),
                                    itemEntity.getProductName(),
                                    itemEntity.getOptionName(),
                                    itemEntity.getPrice()
                            ),
                            itemEntity.getQuantity(),
                            amount
                    )
            );
        }

        responseOrderList.add(
                new OrderResponse(
                        orderEntity.getId(),
                        responseItemList,
                        orderEntity.getOrderCode(),
                        itemsAmount
                )
        );
    }
    */

    @Transactional(readOnly = true)
    @Override
    public OrderRes findOrder(Long userIdx, String orderCode) {

        List<OrderItemRes> responseItemList = new ArrayList<>();
        BigDecimal itemsAmount = BigDecimal.ZERO;

        OrderEntity orderEntity = orderRepository.findByUserIdxAndOrderCode(userIdx, orderCode).orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        List<OrderItemEntity> itemEntities = orderItemRepository.findAllByOrder(orderEntity);

        for(OrderItemEntity entity : itemEntities) {

            BigDecimal amount = entity.getPrice().multiply(BigDecimal.valueOf(entity.getQuantity()));
            itemsAmount = itemsAmount.add(amount);

            responseItemList.add(
                    new OrderItemRes(
                            new ItemInfo(
                                    entity.getProductCode(),
                                    entity.getOptionCode(),
                                    entity.getProductName(),
                                    entity.getOptionName(),
                                    entity.getPrice()
                            ),
                            entity.getQuantity(),
                            amount
                    )
            );
        }

        return new OrderRes(
                orderEntity.getOrderCode(),
                responseItemList,
                itemsAmount
        );
    }

    @Transactional
    @Override
    public void updateOrderStatus(String orderCode, String status) {
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        order.setStatus(OrderStatus.valueOf(status));
        orderRepository.save(order);
        log.info("주문 상태 변경: orderCode={}, status={}", orderCode, status);
    }

    @Transactional(readOnly = true)
    @Override
    public Long findOrderIdx(String orderCode) {
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        return order.getUserIdx();
    }

    @Transactional
    @Override
    public void completeOrder(Long orderId) {
        // 1. 주문 조회
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId=" + orderId));

        // 2. 상태 검증 (PAID 상태에서만 완료 가능)
        if (!order.getStatus().equals(OrderStatus.PAID)) {
            throw new IllegalStateException("결제 완료된 주문만 완료 처리할 수 있습니다. 현재 상태=" + order.getStatus());
        }

        // 3. 주문 상태 변경
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
        log.info("주문 완료 처리: orderId={}", orderId);
    }
}