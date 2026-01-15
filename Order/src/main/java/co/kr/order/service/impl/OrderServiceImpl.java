package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.OutOfStockException;
import co.kr.order.exception.ProductNotFoundException;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.UserData;
import co.kr.order.model.dto.request.CheckStockRequest;
import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.request.ProductRequest;
import co.kr.order.model.dto.request.UserDataRequest;
import co.kr.order.model.dto.response.OrderCartResponse;
import co.kr.order.model.dto.response.OrderDirectResponse;
import co.kr.order.model.dto.response.OrderItemResponse;
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

    private final ProductClient productClient;
    private final UserClient userClient;

    @Transactional
    @Override
    public OrderDirectResponse directOrder(String token, OrderDirectRequest request) {

        // Product 서비스에 feignClient(동기통신) 으로 제품 정보 가져옴
        ProductInfo productInfo;
        try {
            productInfo = productClient.getProduct(new ProductRequest(request.orderRequest().productIdx(), request.orderRequest().optionIdx()));
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 재고 없으면 Error 던짐
        if(productInfo.stock() < request.orderRequest().quantity()) {
            throw new OutOfStockException(ErrorCode.OUT_OF_STOCK);
        }

        // 가격*수량 해서 총 가격 측정
        BigDecimal amount = productInfo.price().multiply(BigDecimal.valueOf(request.orderRequest().quantity()));

        // Member-Service에 동기통신 해서 userIdx, AddressIdx, CardIdx 가져옴
        UserData userData = userClient.getUserData(token, request.userData());

        // Order Table 세팅
        OrderEntity orderEntity = OrderEntity.builder()
                .userIdx(userData.usersIdx())
                .addressIdx(userData.addressIdx())
                .cardIdx(userData.cardIdx())
                .orderCode(UUID.randomUUID().toString())
                .status(OrderStatus.CREATED.name())
                .itemsAmount(amount)
                .totalAmount(amount) // 일단 할인/배송비 고려 x
                .del(false)
                .build();

        orderRepository.save(orderEntity);

        // OrderItem Table 세팅
        OrderItemEntity itemEntity = OrderItemEntity.builder()
                .order(orderEntity)
                .productIdx(productInfo.productIdx())
                .optionIdx(request.orderRequest().optionIdx())
                .productName(productInfo.productName())
                .optionName(productInfo.optionContent())
                .price(productInfo.price())
                .quantity(request.orderRequest().quantity())
                .del(false)
                .build();

        orderItemRepository.save(itemEntity);

        // 재고 관리를 위한 상품에게 몇개 샀는지 정보 전송
        CheckStockRequest stockRequest = new CheckStockRequest(
                request.orderRequest().productIdx(),
                request.orderRequest().optionIdx(),
                request.orderRequest().quantity()
        );
        productClient.checkStock(stockRequest);

        // 상품을 상세 내용
        OrderItemResponse itemInfo = new OrderItemResponse(
                productInfo,
                request.orderRequest().quantity(),
                amount
        );

        // 응답 dto 생성
        return new OrderDirectResponse(
                itemInfo
//                salePrice,
//                shippingFee,
//                totalAmount
        );
    }

    @Transactional
    @Override
    public OrderCartResponse cartOrder(String token, UserDataRequest request) {

        UserData userData = userClient.getUserData(token, request);

        // 주문(Order) 엔티티 생성 및 저장
        BigDecimal itemsAmount = BigDecimal.ZERO;

        OrderEntity orderEntity = OrderEntity.builder()
                .userIdx(userData.usersIdx())
                .addressIdx(userData.addressIdx())
                .cardIdx(userData.cardIdx())
                .orderCode(UUID.randomUUID().toString())
                .status(OrderStatus.CREATED.name())
                .itemsAmount(itemsAmount)
                .totalAmount(itemsAmount) // 추후 배송비 등이 있다면 로직 추가 필요
                .del(false)
                .build();

        orderRepository.save(orderEntity);

        // 내 장바구니 목록 조회
        List<CartEntity> cartEntities = cartRepository.findAllByUserIdx(userData.usersIdx());

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
        List<CheckStockRequest> stockRequests = new ArrayList<>();
        List<OrderItemResponse> responseList = new ArrayList<>();

        for (CartEntity cartEntity : cartEntities) {

            // Map 키 생성
            String key = cartEntity.getProductIdx() + "-" + cartEntity.getOptionIdx();
            ProductInfo info = productMap.get(key);

            // 상품이 존재하지 않을 경우 예외 처리
            if (info == null) {
                throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            // 재고 부족 시 예외 발생
            if (info.stock() < cartEntity.getQuantity()) {
                throw new OutOfStockException(ErrorCode.OUT_OF_STOCK);
            }

            // 가격 계산
            BigDecimal amount = info.price().multiply(BigDecimal.valueOf(cartEntity.getQuantity()));
            itemsAmount = itemsAmount.add(amount);

            // OrderItem 엔티티 생성 (아직 저장은 안 함)
            OrderItemEntity orderItemEntity = OrderItemEntity.builder()
                    .order(orderEntity)
                    .productIdx(cartEntity.getProductIdx())
                    .optionIdx(cartEntity.getOptionIdx())
                    .productName(info.productName())
                    .optionName(info.optionContent())
                    .price(info.price())
                    .quantity(cartEntity.getQuantity())
                    .del(false)
                    .build();
            orderItemsToSave.add(orderItemEntity);

            // 응답 리스트 추가
            responseList.add(new OrderItemResponse(info, cartEntity.getQuantity(), amount));

            // 재고 차감 요청 객체 생성 (product-service용)
            stockRequests.add(new CheckStockRequest(
                    cartEntity.getProductIdx(),
                    cartEntity.getOptionIdx(),
                    cartEntity.getQuantity()
            ));
        }

        // Product-Service에 구매한 수량만큼 재고 감소 요청
        productClient.checkStocks(stockRequests);

        // 주문 상품 일괄 저장 (insert 한번)
        orderItemRepository.saveAll(orderItemsToSave);

        // 주문 총액 업데이트
        orderEntity.setItemsAmount(itemsAmount);
        // orderEntity.setTotalAmount(...); // 일단 totalAmount는 안하니까
        orderRepository.save(orderEntity);

        // 장바구니 비우기
        cartRepository.deleteByUserIdx(userData.usersIdx());

        return new OrderCartResponse(
                responseList,
                itemsAmount
//                salePrice,
//                shippingFee,
//                totalAmount
        );
    }
}