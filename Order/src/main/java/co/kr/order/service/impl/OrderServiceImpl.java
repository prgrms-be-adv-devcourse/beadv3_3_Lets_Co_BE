package co.kr.order.service.impl;

import co.kr.order.client.PaymentClient;
import co.kr.order.client.ProductClient;
import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.OrderNotFoundException;
import co.kr.order.exception.OutOfStockException;
import co.kr.order.exception.ProductNotFoundException;
import co.kr.order.model.dto.ItemInfo;
import co.kr.order.model.dto.request.*;
import co.kr.order.model.dto.response.*;
import co.kr.order.model.entity.OrderEntity;
import co.kr.order.model.entity.OrderItemEntity;
import co.kr.order.model.entity.SettlementHistoryEntity;
import co.kr.order.model.vo.OrderStatus;
import co.kr.order.model.vo.PaymentType;
import co.kr.order.model.vo.SettlementType;
import co.kr.order.repository.OrderItemJpaRepository;
import co.kr.order.repository.OrderJpaRepository;
import co.kr.order.repository.SettlementRepository;
import co.kr.order.service.OrderService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final SettlementRepository settlementRepository;

    private final CartServiceImpl cartService;

    private final PaymentClient paymentClient;
    private final ProductClient productClient;


    @Transactional
    @Override
    public OrderRes directOrder(Long userIdx, OrderDirectReq request) {

        // Product 서비스에 feignClient(동기통신) 으로 제품 정보 가져옴
        ClientProductRes productResponse;
        try {
            productResponse = productClient.getProduct(request.productInfo().productCode(), request.productInfo().optionCode());
        } catch (FeignException.NotFound e) {
            // 오류 받으면 error
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 재고 없으면 Error 던짐
        if (productResponse.stock() < request.productInfo().quantity()) {
            throw new OutOfStockException(ErrorCode.OUT_OF_STOCK);
        }

        // 가격*수량 해서 총 가격 측정
        BigDecimal amount = productResponse.price().multiply(BigDecimal.valueOf(request.productInfo().quantity()));

        // orderCode 생성
        String orderCode = UUID.randomUUID().toString();

        // Order Table 세팅
        OrderEntity orderEntity = OrderEntity.builder()
                .userIdx(userIdx)
                .orderCode(orderCode)
                .recipient(request.addressInfo().recipient())
                .address(request.addressInfo().address())
                .addressDetail(request.addressInfo().addressDetail())
                .phone(request.addressInfo().phone())
                .itemsAmount(amount)
                .totalAmount(amount) // 일단 할인/배송비 고려 x
                .build();
        orderRepository.save(orderEntity);

        // OrderItem Table 세팅
        OrderItemEntity itemEntity = OrderItemEntity.builder()
                .order(orderEntity)
                .productIdx(productResponse.productIdx())
                .optionIdx(productResponse.optionIdx())
                .productName(productResponse.productName())
                .optionName(productResponse.optionName())
                .price(productResponse.price())
                .quantity(request.productInfo().quantity())
                .del(false)
                .build();
        orderItemRepository.save(itemEntity);

        ClientPaymentRes pay = paymentClient.processPayment(
                new ClientPaymentReq(
                        userIdx,
                        orderCode,
                        orderEntity.getId(),
                        request.paymentType(),
                        amount,
                        request.tossKey() // CARD/DEPOSIT은 null, TOSS_PAY는 paymentKey
                )
        );
        orderEntity.setStatus(OrderStatus.PAID);

        saveSettlementHistory(productResponse.sellerIdx(), pay.paymentIdx(), amount);

        // Product-Service에 구매한 수량만큼 재고 감소 요청
        DeductStockReq stockRequest = new DeductStockReq(
                productResponse.productIdx(),
                productResponse.optionIdx(),
                request.productInfo().quantity()
        );

        try {
            // Product-Service에 구매한 수량만큼 재고 감소 요청
            productClient.deductStocks(List.of(stockRequest));

        } catch (Exception e) {
            // 비상 상황: 돈은 나갔는데 재고 차감이나 후처리가 실패 (여러 사람이 1개의 재고를 동시에 주문했을 경우)
            // 반드시 결제를 취소(환불) 해줘야 함

            // TOSS_PAY는 아직 결제 전이므로 주문 상태만 취소로 변경 (혹은 삭제)
            if (request.paymentType() != PaymentType.TOSS_PAY) {
                // 이미 돈이 나간 경우에만 환불 처리
                paymentClient.refundPayment(
                        new ClientRefundReq(userIdx, orderCode)
                );
                orderEntity.setStatus(OrderStatus.REFUNDED);
            }

            // 예외를 다시 던져서 DB 트랜잭션(주문 생성 등)을 롤백시킴
            throw e;
        }

        // 상품을 상세 내용
        OrderItemRes itemInfo = new OrderItemRes(
                new ItemInfo(
                        productResponse.productIdx(),
                        productResponse.optionIdx(),
                        productResponse.productName(),
                        productResponse.optionName(),
                        productResponse.price()
                ),
                request.productInfo().quantity(),
                amount
        );

        orderRepository.save(orderEntity);

        return new OrderRes(
                orderEntity.getId(),
                List.of(itemInfo),
                orderEntity.getOrderCode(),
                amount
        );
    }

    @Transactional
    @Override
    public OrderRes cartOrder(Long userIdx, OrderCartReq request) {

        String orderCode = UUID.randomUUID().toString();
        BigDecimal itemsAmount = BigDecimal.ZERO;

        OrderEntity orderEntity = OrderEntity.builder()
                .userIdx(userIdx)
                .orderCode(orderCode)
                .recipient(request.addressInfo().recipient())
                .address(request.addressInfo().address())
                .addressDetail(request.addressInfo().addressDetail())
                .phone(request.addressInfo().phone())
                .itemsAmount(itemsAmount)
                .totalAmount(itemsAmount)
                .build();

        orderRepository.save(orderEntity);

        List<CartItemRes> cartList = cartService.getCartList(userIdx);
        if (cartList.isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어있습니다.");
        }

        List<ClientProductReq> cartRequest = new ArrayList<>();
        for (CartItemRes cart : cartList) {
            ClientProductReq req =
                    new ClientProductReq(cart.product().productIdx(), cart.product().optionIdx());
            cartRequest.add(req);
        }

        Set<Long> productIdSet = new HashSet<>();
        for (CartItemRes cart : cartList) {
            productIdSet.add(cart.product().productIdx());
        }
        List<Long> productIds = new ArrayList<>(productIdSet);

        List<ClientProductRes> productResponse;
        try {
            productResponse = productClient.getProductList(cartRequest);
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 상품별 판매자 ID 조회
        Map<Long, Long> productSellerMap = productClient.getSellersByProductIds(productIds);

        // 조회된 리스트를 Map으로 변환 (Key: "상품ID-옵션ID")
        Map<String, ClientProductRes> productMap = new HashMap<>();
        for (ClientProductRes info : productResponse) {
            String key = info.productIdx() + "-" + info.optionIdx();
            productMap.put(key, info);
        }

        // 데이터 가공 및 정산 금액 합산 준비
        List<OrderItemEntity> orderItemsToSave = new ArrayList<>();
        List<DeductStockReq> stockRequests = new ArrayList<>();
        List<OrderItemRes> responseList = new ArrayList<>();
        Map<Long, BigDecimal> sellerAmountMap = new HashMap<>(); // 판매자별 정산금 합산용

        for (CartItemRes cartItem : cartList) {

            String key = cartItem.product().productIdx() + "-" + cartItem.product().optionIdx();
            ClientProductRes product = productMap.get(key);

            if (product == null) {
                throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            if (product.stock() < cartItem.quantity()) {
                throw new OutOfStockException(ErrorCode.OUT_OF_STOCK);
            }

            BigDecimal amount = product.price().multiply(BigDecimal.valueOf(cartItem.quantity()));
            itemsAmount = itemsAmount.add(amount);

            // OrderItem 생성
            OrderItemEntity orderItemEntity = OrderItemEntity.builder()
                    .order(orderEntity)
                    .productIdx(cartItem.product().productIdx())
                    .optionIdx(cartItem.product().optionIdx())
                    .productName(product.productName())
                    .optionName(product.optionName())
                    .price(product.price())
                    .quantity(cartItem.quantity())
                    .del(false)
                    .build();
            orderItemsToSave.add(orderItemEntity);

            // 응답 리스트 추가
            responseList.add(new OrderItemRes(
                    new ItemInfo(
                            product.productIdx(),
                            product.optionIdx(),
                            product.productName(),
                            product.optionName(),
                            product.price()
                    ),
                    cartItem.quantity(),
                    amount
            ));

            // 재고 차감 요청 리스트 추가
            stockRequests.add(new DeductStockReq(
                    cartItem.product().productIdx(),
                    cartItem.product().optionIdx(),
                    cartItem.quantity()
            ));

            // 판매자별 정산 금액 합산 로직
            Long sellerIdx = productSellerMap.get(cartItem.product().productIdx());
            if (sellerIdx != null) {
                sellerAmountMap.merge(sellerIdx, amount, BigDecimal::add);
            }
        }

        // 주문 상품 DB 저장
        orderItemRepository.saveAll(orderItemsToSave);

        // 주문 총액 업데이트
        orderEntity.setItemsAmount(itemsAmount);
        orderEntity.setTotalAmount(itemsAmount);
        orderRepository.save(orderEntity);

        // 결제 처리
        ClientPaymentRes pay = paymentClient.processPayment(
                new ClientPaymentReq(
                        userIdx,
                        orderCode,
                        orderEntity.getId(),
                        request.paymentType(),
                        itemsAmount,
                        request.tossKey() // CARD/DEPOSIT은 null, TOSS_PAY는 paymentKey
                )
        );
        orderEntity.setStatus(OrderStatus.PAID);

        // 판매자별 정산 내역 저장 (Loop)
        for (Map.Entry<Long, BigDecimal> entry : sellerAmountMap.entrySet()) {
            saveSettlementHistory(entry.getKey(), pay.paymentIdx(), entry.getValue());
        }

        // 재고 차감 및 장바구니 비우기 (실패 시 보상 트랜잭션 수행)
        try {
            productClient.deductStocks(stockRequests);
        } catch (Exception e) {
            log.error("주문 후처리 실패 (재고 차감 등). 환불 진행. orderCode={}", orderCode, e);

            // TOSS_PAY는 승인이 완료된 상태이므로 취소 필요
            // 다른 결제 수단도 돈이 나갔다면 환불 처리
            if (request.paymentType() != PaymentType.TOSS_PAY) {
                paymentClient.refundPayment(
                        new ClientRefundReq(userIdx, orderCode)
                );
            }
            else {
                // Toss 결제도 승인 후 실패 시 환불(취소) 처리 필요
                // (Toss는 승인 API 호출 성공 시 돈이 나간 상태임)
                paymentClient.refundPayment(
                        new ClientRefundReq(userIdx, orderCode)
                );
            }
            orderEntity.setStatus(OrderStatus.REFUNDED);

            throw e;
        }

        try {
            cartService.deleteCartAll(userIdx);
        } catch (Exception e) {
            // 여기서 에러가 나도 전체 주문을 롤백(환불)할 필요는 없음
            // 로그만 남기고 운영자가 나중에 확인하거나 무시
            log.error("주문은 성공했으나 장바구니 비우기 실패 Redis Error userIdx={}", userIdx, e);
        }

        orderRepository.save(orderEntity);

        return new OrderRes(
                orderEntity.getId(),
                responseList,
                orderEntity.getOrderCode(),
                itemsAmount);
    }


    private void saveSettlementHistory(Long sellerIdx, Long paymentIdx, BigDecimal amount) {

        SettlementHistoryEntity entity = SettlementHistoryEntity.builder()
                .sellerIdx(sellerIdx)
                .paymentIdx(paymentIdx)
                .type(SettlementType.Orders_CONFIRMED)
                .amount(amount)
                .build();
        settlementRepository.save(entity);
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

            return new OrderRes(
                    orderEntity.getId(),
                    responseItemList,
                    orderEntity.getOrderCode(),
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
                                    entity.getProductIdx(),
                                    entity.getOptionIdx(),
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
                orderEntity.getId(),
                responseItemList,
                orderEntity.getOrderCode(),
                itemsAmount
        );
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

    @Transactional
    @Override
    public void updateOrderStatus(String orderCode, String status) {
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        order.setStatus(OrderStatus.valueOf(status));
        orderRepository.save(order);
        log.info("주문 상태 변경: orderCode={}, status={}", orderCode, status);
    }
}