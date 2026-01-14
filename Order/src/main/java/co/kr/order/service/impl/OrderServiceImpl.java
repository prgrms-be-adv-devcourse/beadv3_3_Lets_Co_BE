package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.OutOfStockException;
import co.kr.order.exception.ProductNotFoundException;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.UserData;
import co.kr.order.model.dto.request.*;
import co.kr.order.model.dto.response.OrderCartResponse;
import co.kr.order.model.dto.response.OrderDirectResponse;
import co.kr.order.model.dto.response.OrderItemResponse;
import co.kr.order.model.entity.OrderEntity;
import co.kr.order.model.entity.OrderItemEntity;
import co.kr.order.model.vo.OrderStatus;
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
    public OrderCartResponse cartOrder(String token, OrderCartRequest request) {

        // usersIdx, addressIdx, cardIdx
        UserData userData = userClient.getUserData(token, request.userdata());

        // itemAmount가 notnull 이기 때문에 0으로 초기화 할 값
        BigDecimal itemsAmount = BigDecimal.ZERO;

        // Order Table 세팅
        OrderEntity orderEntity = OrderEntity.builder()
                .userIdx(userData.usersIdx())
                .addressIdx(userData.addressIdx())
                .cardIdx(userData.cardIdx())
                .orderCode(UUID.randomUUID().toString())
                .status(OrderStatus.CREATED.name())
                .itemsAmount(itemsAmount)
                .totalAmount(itemsAmount)
                .del(false)
                .build();

        orderRepository.save(orderEntity);

        // Product-Service에 보낼 데이터 묶기
        List<ProductRequest> productList = new ArrayList<>();
        for (OrderRequest order : request.orderList()) {
            ProductRequest productRequest = new ProductRequest(order.productIdx(), order.optionIdx());
            productList.add(productRequest);
        }

        // dto로 한번에 전송 (bulk)
        List<ProductInfo> productInfos;
        try {
            productInfos = productClient.getProductList(productList);
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 받아온 ProductInfo 리스트를 Map으로 변환
        // Key: productIdx-optionIdx, Value: ProductInfo 객체
        Map<String, ProductInfo> productMap = new HashMap<>();
        for (ProductInfo info : productInfos) {
            // 예시 100-10
            String key = info.productIdx() + "-" + info.optionIdx();
            productMap.put(key, info);
        }

        // 반복문 마다 save() 하는걸 방지하기 위해
        // DB에 저장할 엔티티들을 모아둘 리스트 & 클라이언트에게 줄 응답 리스트
        List<OrderItemEntity> orderItemsToSave = new ArrayList<>();
        List<OrderItemResponse> responseList = new ArrayList<>();

        // 몇개 샀는지 product-service에게 보낼 list
        List<CheckStockRequest> stockRequests = new ArrayList<>();

        for(OrderRequest order : request.orderList()) {

            // Map에서 상품 정보 꺼내기
            String key = order.productIdx() + "-" + order.optionIdx();
            ProductInfo info = productMap.get(key);

            if (info == null) {
                throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            // 재고 없으면 Error 던짐
            if (info.stock() < order.quantity()) {
                throw new OutOfStockException(ErrorCode.OUT_OF_STOCK);
            }

            // 주문 값 계산
            BigDecimal amount = info.price().multiply(BigDecimal.valueOf(order.quantity()));
            itemsAmount = itemsAmount.add(amount);

            // OrderItem Table 세팅
            OrderItemEntity itemEntity = OrderItemEntity.builder()
                    .order(orderEntity)
                    .productIdx(info.productIdx())
                    .optionIdx(order.optionIdx())
                    .productName(info.productName())
                    .optionName(info.optionContent())
                    .price(info.price())
                    .quantity(order.quantity())
                    .del(false)
                    .build();

            orderItemsToSave.add(itemEntity);

            responseList.add(new OrderItemResponse(info, order.quantity(), amount));

            // 재고 객체 생성해서 리스트에 추가
            stockRequests.add(
                    new CheckStockRequest(
                        order.productIdx(),
                        order.optionIdx(),
                        order.quantity()
                    )
            );
        }

        // product-service에게 얼마나 구매했는지 전송
        productClient.checkStocks(stockRequests);

        // 모아둔 엔티티를 한 번에 저장 (DB Insert 1회)
        orderItemRepository.saveAll(orderItemsToSave);

        orderEntity.setItemsAmount(itemsAmount);
//        orderEntity.setTotalAmount(tempAmount);
        orderRepository.save(orderEntity);

        return new OrderCartResponse(
                responseList,
                itemsAmount
//                salePrice,
//                shippingFee,
//                totalAmount
        );
    }
}