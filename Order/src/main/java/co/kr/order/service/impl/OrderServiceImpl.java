package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.exception.*;
import co.kr.order.model.dto.DeductStock;
import co.kr.order.model.dto.ItemInfo;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.UserData;
import co.kr.order.model.dto.request.OrderCartRequest;
import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.request.PaymentRequest;
import co.kr.order.model.dto.request.ProductRequest;
import co.kr.order.model.dto.response.OrderItemResponse;
import co.kr.order.model.dto.response.OrderResponse;
import co.kr.order.model.entity.CartEntity;
import co.kr.order.model.entity.OrderEntity;
import co.kr.order.model.entity.OrderItemEntity;
import co.kr.order.model.vo.OrderStatus;
import co.kr.order.model.vo.PaymentType;
import co.kr.order.repository.CartJpaRepository;
import co.kr.order.repository.OrderItemJpaRepository;
import co.kr.order.repository.OrderJpaRepository;
import co.kr.order.service.OrderService;
import co.kr.order.service.SettlementService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final CartJpaRepository cartRepository;

    private final PaymentServiceImpl paymentService;

    private final ProductClient productClient;
    private final UserClient userClient;
    private final SettlementService settlementService;

    @Transactional
    @Override
    public OrderResponse directOrder(Long userIdx, OrderDirectRequest request) {

        // 주소 데이터 있는지 확인
        if (request.userData().addressInfo() == null) {
            throw new NoInputAddressDataException(ErrorCode.NO_INPUT_ADDRESS_DATA);
        }

        // Product 서비스에 feignClient(동기통신) 으로 제품 정보 가져옴
        ProductInfo product;
        try {
            product = productClient.getProduct(request.orderItem().productIdx(), request.orderItem().optionIdx());
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 재고 없으면 Error 던짐
        if(product.stock() < request.orderItem().quantity()) {
            throw new OutOfStockException(ErrorCode.OUT_OF_STOCK);
        }

        // 가격*수량 해서 총 가격 측정
        BigDecimal amount = product.price().multiply(BigDecimal.valueOf(request.orderItem().quantity()));

        // Member-Service에 동기통신 해서 userIdx, AddressIdx, CardIdx 가져온 후 값있는지 확인
        UserData userData = userClient.getUserData(userIdx, request.userData());

        // orderCode 생성
        String orderCode = UUID.randomUUID().toString();

        // Order Table 세팅
        OrderEntity orderEntity = OrderEntity.builder()
                .userIdx(userIdx)
                .addressIdx(userData.addressInfo().addressIdx())
                .cardIdx(userData.cardInfo().cardIdx())
                .orderCode(orderCode)
                .status(OrderStatus.CREATED)
                .itemsAmount(amount)
                .totalAmount(amount) // 일단 할인/배송비 고려 x
                .del(false)
                .build();

        orderRepository.save(orderEntity);

        // OrderItem Table 세팅
        OrderItemEntity itemEntity = OrderItemEntity.builder()
                .order(orderEntity)
                .productIdx(product.productIdx())
                .optionIdx(request.orderItem().optionIdx())
                .productName(product.productName())
                .optionName(product.optionName())
                .price(product.price())
                .quantity(request.orderItem().quantity())
                .del(false)
                .build();

        orderItemRepository.save(itemEntity);

        if (request.paymentType() != PaymentType.TOSS_PAY) {
            paymentService.pay(
                    userIdx,
                    new PaymentRequest(
                            orderCode,
                            request.paymentType()
                    )
            );
        }

        // Product-Service에 구매한 수량만큼 재고 감소 요청
        DeductStock stockRequest = new DeductStock(
                request.orderItem().productIdx(),
                request.orderItem().optionIdx(),
                request.orderItem().quantity()
        );

        try {
            // Product-Service에 구매한 수량만큼 재고 감소 요청
            productClient.deductStock(stockRequest);

        } catch (Exception e) {
            // 비상 상황: 돈은 나갔는데 재고 차감이나 후처리가 실패 (여러 사람이 1개의 재고를 동시에 주문했을 경우)
            // 반드시 결제를 취소(환불) 해줘야 함

            // TOSS_PAY는 아직 결제 전이므로 주문 상태만 취소로 변경 (혹은 삭제)
            if (request.paymentType() != PaymentType.TOSS_PAY) {
                // 이미 돈이 나간 경우에만 환불 처리
                paymentService.refund(userIdx, orderCode);
            }

            // 예외를 다시 던져서 DB 트랜잭션(주문 생성 등)을 롤백시킴
            throw e;
        }

        // 상품을 상세 내용
        OrderItemResponse itemInfo = new OrderItemResponse(
                new ItemInfo(
                        product.productIdx(),
                        product.optionIdx(),
                        product.productName(),
                        product.optionName(),
                        product.price()
                ),
                request.orderItem().quantity(),
                amount
        );

        return new OrderResponse(
                List.of(itemInfo),
                amount
        );
    }

    @Transactional
    @Override
    public OrderResponse cartOrder(Long userIdx, OrderCartRequest request) {

        UserData userData = userClient.getUserData(userIdx, request.userData());

        if (request.userData().addressInfo() == null) {
            throw new NoInputAddressDataException(ErrorCode.NO_INPUT_ADDRESS_DATA);
        }

        String orderCode = UUID.randomUUID().toString();
        BigDecimal itemsAmount = BigDecimal.ZERO;

        OrderEntity orderEntity = OrderEntity.builder()
                .userIdx(userIdx)
                .addressIdx(userData.addressInfo().addressIdx())
                .cardIdx(userData.cardInfo().cardIdx())
                .orderCode(orderCode)
                .status(OrderStatus.CREATED)
                .itemsAmount(itemsAmount)
                .totalAmount(itemsAmount) // 추후 배송비 등이 있다면 로직 추가 필요
                .del(false)
                .build();

        orderRepository.save(orderEntity);

        // 내 장바구니 목록 조회
        List<CartEntity> cartEntities = cartRepository.findAllByUserIdx(userIdx);

        // 장바구니가 비어있을 경우 예외처리
        if (cartEntities.isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어있습니다.");
        }

        // Product-Service에 보낼 상품 ID 목록 추출 (Bulk 조회용)
        List<ProductRequest> productRequests = new ArrayList<>();
        for (CartEntity cart : cartEntities) {
            productRequests.add(new ProductRequest(cart.getProductIdx(), cart.getOptionIdx()));
        }

        // 상품 정보 한 번에 조회 (Bulk Fetch)
        List<ProductInfo> productInfos;
        try {
            productInfos = productClient.getProductList(productRequests);
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 조회된 리스트를 Map으로 변환
        // Key: productIdx-optionIdx, Value: ProductInfo
        Map<String, ProductInfo> productMap = new HashMap<>();
        for (ProductInfo info : productInfos) {
            // Map에 저장 (Key: "상품ID-옵션ID", Value: ProductInfo 객체)
            String key = info.productIdx() + "-" + info.optionIdx();
            productMap.put(key, info);
        }
        // DB에 저장할 엔티티 리스트 & 재고 차감 요청 리스트 &  응답용 리스트
        List<OrderItemEntity> orderItemsToSave = new ArrayList<>();
        List<DeductStock> stockRequests = new ArrayList<>();
        List<OrderItemResponse> responseList = new ArrayList<>();

        for (CartEntity cartEntity : cartEntities) {

            // Map 키 생성
            String key = cartEntity.getProductIdx() + "-" + cartEntity.getOptionIdx();
            ProductInfo product = productMap.get(key);

            // 상품이 존재하지 않을 경우 예외 처리
            if (product == null) {
                throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            // 재고 부족 시 예외 발생
            if (product.stock() < cartEntity.getQuantity()) {
                throw new OutOfStockException(ErrorCode.OUT_OF_STOCK);
            }

            // 가격 계산
            BigDecimal amount = product.price().multiply(BigDecimal.valueOf(cartEntity.getQuantity()));
            itemsAmount = itemsAmount.add(amount);

            // OrderItem 엔티티 생성 (아직 저장은 안 함)
            OrderItemEntity orderItemEntity = OrderItemEntity.builder()
                    .order(orderEntity)
                    .productIdx(cartEntity.getProductIdx())
                    .optionIdx(cartEntity.getOptionIdx())
                    .productName(product.productName())
                    .optionName(product.optionName())
                    .price(product.price())
                    .quantity(cartEntity.getQuantity())
                    .del(false)
                    .build();
            orderItemsToSave.add(orderItemEntity);

            // 응답 리스트 추가
            responseList.add(
                    new OrderItemResponse(
                        new ItemInfo(
                                product.productIdx(),
                                product.optionIdx(),
                                product.productName(),
                                product.optionName(),
                                product.price()
                        ),
                        cartEntity.getQuantity(),
                        amount
                    )
            );

            // 재고 차감 요청 객체 생성 (product-service용)
            stockRequests.add(new DeductStock(
                    cartEntity.getProductIdx(),
                    cartEntity.getOptionIdx(),
                    cartEntity.getQuantity()
            ));
        }

        // 주문 상품 일괄 저장 (insert 한번)
        orderItemRepository.saveAll(orderItemsToSave);

        // 주문 총액 업데이트
        orderEntity.setItemsAmount(itemsAmount);
        // orderEntity.setTotalAmount(...); // 일단 totalAmount는 안하니까
        orderRepository.save(orderEntity);

        // TOSS_PAY가 아닐 때만 즉시 결제 시도
        if (request.paymentType() != PaymentType.TOSS_PAY) {
            paymentService.pay(
                    userIdx,
                    new PaymentRequest(
                            orderCode,
                            request.paymentType()
                    )
            );
        }

        try {
            // Product-Service에 구매한 수량만큼 재고 감소 요청
            productClient.deductStocks(stockRequests);
            // 주문완료 후 카트내역 삭제
            cartRepository.deleteByUserIdx(userIdx);

        } catch (Exception e) {
            // 비상 상황: 돈은 나갔는데 재고 차감이나 후처리가 실패 (여러 사람이 1개의 재고를 동시에 주문했을 경우)
            // 반드시 결제를 취소(환불) 해줘야 함

            // TOSS_PAY는 결제 전이므로 별도 환불 로직 불필요 (트랜잭션 롤백)
            if (request.paymentType() != PaymentType.TOSS_PAY) {
                // 이미 돈이 나간 경우(DEPOSIT 등)에만 환불 처리
                paymentService.refund(userIdx, orderCode);
            }

            // 예외를 다시 던져서 DB 트랜잭션(주문 생성 등)을 롤백시킴
            throw e;
        }

        return new OrderResponse(
                responseList,
                itemsAmount
//                salePrice,
//                shippingFee,
//                totalAmount
        );
    }

    @Transactional
    @Override
    public String refund(Long userIdx, String orderCode) {

        OrderEntity orderEntity = orderRepository.findByOrderCode(orderCode).orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        paymentService.refund(userIdx, orderCode);
        orderEntity.setStatus(OrderStatus.REFUNDED);

        return "환불처리가 완료 되었습니다.";
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderResponse> findOrderList(Long userIdx) {

        List<OrderResponse> responseOrderList = new ArrayList<>();

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
                            responseItemList,
                            itemsAmount
                    )
            );
        }

        return responseOrderList;
    }

    @Transactional(readOnly = true)
    @Override
    public OrderResponse findOrder(Long userIdx, String orderCode) {

        List<OrderItemResponse> responseItemList = new ArrayList<>();
        BigDecimal itemsAmount = BigDecimal.ZERO;

        OrderEntity orderEntity = orderRepository.findByUserIdxAndOrderCode(userIdx, orderCode).orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        List<OrderItemEntity> itemEntities = orderItemRepository.findAllByOrder(orderEntity);

        for(OrderItemEntity entity : itemEntities) {

            BigDecimal amount = entity.getPrice().multiply(BigDecimal.valueOf(entity.getQuantity()));
            itemsAmount = itemsAmount.add(amount);

            responseItemList.add(
                    new OrderItemResponse(
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

        return new OrderResponse(responseItemList, itemsAmount);
    }

    /**
     * 주문 완료 처리
     * - 결제 완료(PAID) 상태의 주문만 완료 처리 가능
     * - 주문 상태를 COMPLETED로 변경
     * - 정산 생성
     *
     * @param orderId 주문 ID
     */
    @Transactional
    @Override
    public void completeOrder(Long orderId) {
        // 1. 주문 조회
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId=" + orderId));

        // 2. 상태 검증 (PAID 상태에서만 완료 가능)
        if (!OrderStatus.PAID.name().equals(order.getStatus())) {
            throw new IllegalStateException("결제 완료된 주문만 완료 처리할 수 있습니다. 현재 상태=" + order.getStatus());
        }

        // 3. 주문 상태 변경
        order.setOrderStatus(OrderStatus.COMPLETED.name());
        orderRepository.save(order);
        log.info("주문 완료 처리: orderId={}", orderId);

        // 4. 정산 생성
        settlementService.createSettlement(orderId);
    }
}