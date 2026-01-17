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
import co.kr.order.model.dto.response.OrderListResponse;
import co.kr.order.model.dto.response.OrderResponse;
import co.kr.order.model.entity.CartEntity;
import co.kr.order.model.entity.OrderEntity;
import co.kr.order.model.entity.OrderItemEntity;
import co.kr.order.model.vo.OrderStatus;
import co.kr.order.repository.CartJpaRepository;
import co.kr.order.repository.OrderItemJpaRepository;
import co.kr.order.repository.OrderJpaRepository;
import co.kr.order.service.OrderService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final CartJpaRepository cartRepository;

    private final PaymentServiceImpl paymentService;

    private final ProductClient productClient;
    private final UserClient userClient;

    @Transactional
    @Override
    public OrderResponse directOrder(Long userIdx, OrderDirectRequest request) {

        // 주소/카드 데이터 있는지 확인
        validateOrderData(request.userData());

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
                .status(OrderStatus.CREATED.name())
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
                .optionName(product.optionContent())
                .price(product.price())
                .quantity(request.orderItem().quantity())
                .del(false)
                .build();

        orderItemRepository.save(itemEntity);

        // 실제 결제 진행
        paymentService.pay(
                userIdx,
                new PaymentRequest(
                        orderCode,
                        request.paymentType()
                )
        );

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
            paymentService.refund(userIdx, orderCode);

            // 예외를 다시 던져서 DB 트랜잭션(주문 생성 등)을 롤백시킴
            throw e;
        }

        // 상품을 상세 내용
        OrderItemResponse itemInfo = new OrderItemResponse(
                new ItemInfo(
                        product.productIdx(),
                        product.optionIdx(),
                        product.productName(),
                        product.optionContent(),
                        product.price()
                ),
                request.orderItem().quantity(),
                amount
        );

        // 응답 dto 생성
        return new OrderResponse(
                itemInfo
//                salePrice,
//                shippingFee,
//                totalAmount
        );
    }

    @Transactional
    @Override
    public OrderListResponse cartOrder(Long userIdx, OrderCartRequest request) {

        UserData userData = userClient.getUserData(userIdx, request.userData());
        validateOrderData(userData);

        String orderCode = UUID.randomUUID().toString();
        BigDecimal itemsAmount = BigDecimal.ZERO;

        OrderEntity orderEntity = OrderEntity.builder()
                .userIdx(userIdx)
                .addressIdx(userData.addressInfo().addressIdx())
                .cardIdx(userData.cardInfo().cardIdx())
                .orderCode(orderCode)
                .status(OrderStatus.CREATED.name())
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
                    .optionName(product.optionContent())
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
                                product.optionContent(),
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

        // 실제 결제 진행
        paymentService.pay(
                userIdx,
                new PaymentRequest(
                        orderCode,
                        request.paymentType()
                )
        );

        try {
            // Product-Service에 구매한 수량만큼 재고 감소 요청
            productClient.deductStocks(stockRequests);
            // 주문완료 후 카트내역 삭제
            cartRepository.deleteByUserIdx(userIdx);

        } catch (Exception e) {
            // 비상 상황: 돈은 나갔는데 재고 차감이나 후처리가 실패 (여러 사람이 1개의 재고를 동시에 주문했을 경우)
            // 반드시 결제를 취소(환불) 해줘야 함
            paymentService.refund(userIdx, orderCode);

            // 예외를 다시 던져서 DB 트랜잭션(주문 생성 등)을 롤백시킴
            throw e;
        }

        return new OrderListResponse(
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
        orderEntity.setStatus(OrderStatus.REFUNDED.name());

        return "환불처리 되었습니다.";
    }

    @Transactional(readOnly = true)
    @Override
    public OrderListResponse findOrderList(Long userIdx) {

        List<OrderEntity> orderEntities = orderRepository.findAllByUserIdx(userIdx);

        if (orderEntities.isEmpty()) {
            return new OrderListResponse(Collections.emptyList(), BigDecimal.ZERO);
        }

        List<OrderItemEntity> itemEntities = orderItemRepository.findAllByOrderIn(orderEntities);

        List<OrderItemResponse> itemList = new ArrayList<>();
        for (OrderItemEntity item : itemEntities) {
            BigDecimal itemTotalAmount = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            itemList.add(new OrderItemResponse(
                    new ItemInfo(
                            item.getProductIdx(),
                            item.getOptionIdx(),
                            item.getProductName(),
                            item.getOptionName(),
                            item.getPrice()
                    ),
                    item.getQuantity(),
                    itemTotalAmount
            ));
        }

        BigDecimal totalItemsAmount = BigDecimal.ZERO;

        for (OrderEntity order : orderEntities) {
            if (order.getItemsAmount() != null) {
                totalItemsAmount = totalItemsAmount.add(order.getItemsAmount());
            }
        }

        return new OrderListResponse(
                itemList,
                totalItemsAmount);
    }


    @Transactional(readOnly = true)
    @Override
    public OrderResponse findOrder(Long userIdx, String orderCode) {

        OrderEntity orderEntity = orderRepository.findByUserIdxAndOrderCode(userIdx, orderCode).orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        List<OrderItemEntity> itemEntities = orderItemRepository.findAllByOrder(orderEntity);

        if (itemEntities.isEmpty()) {
            throw new OrderItemNotFoundException(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        // 단일 Item만 받으므로 첫 번째 상품 가져옴
        OrderItemEntity itemEntity = itemEntities.get(0);
        BigDecimal itemTotalAmount = itemEntity.getPrice().multiply(BigDecimal.valueOf(itemEntity.getQuantity()));

        return new OrderResponse(
                new OrderItemResponse(
                        new ItemInfo(
                                itemEntity.getProductIdx(),
                                itemEntity.getOptionIdx(),
                                itemEntity.getProductName(),
                                itemEntity.getOptionName(),
                                itemEntity.getPrice()
                        ),
                        itemEntity.getQuantity(),
                        itemTotalAmount
                )
        );
    }

    // 주소/카드 정보 비어있는지 확인
    private void validateOrderData(UserData userData) {
        if (userData.addressInfo() == null && userData.cardInfo() == null) {
            throw new NoInputOrderDataException(ErrorCode.NO_INPUT_ORDER_DATA);
        }
        else if (userData.addressInfo() == null) {
            throw new NoInputAddressDataException(ErrorCode.NO_INPUT_ADDRESS_DATA);
        }
        else if (userData.cardInfo() == null) {
            throw new NoInputCardDataException(ErrorCode.NO_INPUT_CARD_DATA);
        }
    }
}