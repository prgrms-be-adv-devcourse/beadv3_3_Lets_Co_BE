package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.OutOfStockException;
import co.kr.order.exception.ProductNotFoundException;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.UserData;
import co.kr.order.model.dto.request.OrderCartRequest;
import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.request.OrderRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;

    private final ProductClient productClient;
    private final UserClient userClient;

    /*
     * Todo
     * N+1 문제
     * 카트/단일 주문의 중복 로직을 통합
     * setter 를 builder 로 변경 -> 완료
     */


    @Transactional
    @Override
    public OrderDirectResponse directOrder(String token, OrderDirectRequest request) {

        // Product 서비스에 feignClient(동기통신) 으로 제품 정보 가져옴
        ProductInfo productInfo;
        try {
            productInfo = productClient.getProduct(request.orderRequest().productIdx(), request.orderRequest().optionIdx());
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

        // todo: 유저가 카드/주소 데이터(DTO)를 body에 넣었을 때 Member-service에 데이터 전송

        // 초반 로직 directOrder와 동일
        UserData userData = userClient.getUserData(token, request.userdata());

        // response에 담길 리스트
        List<OrderItemResponse> itemList = new ArrayList<>();
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

        List<OrderRequest> items = request.orderList();
        for(OrderRequest item : items) {

            ProductInfo productInfo;
            try {
                productInfo = productClient.getProduct(item.productIdx(), item.optionIdx());
            } catch (FeignException.NotFound e) {
                throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            if(productInfo.stock() < item.quantity()) {
                throw new OutOfStockException(ErrorCode.OUT_OF_STOCK);
            }

            BigDecimal amount = productInfo.price().multiply(BigDecimal.valueOf(item.quantity()));

            // OrderItem Table 세팅
            OrderItemEntity itemEntity = OrderItemEntity.builder()
                    .order(orderEntity)
                    .productIdx(productInfo.productIdx())
                    .optionIdx(item.optionIdx())
                    .productName(productInfo.productName())
                    .optionName(productInfo.optionContent())
                    .price(productInfo.price())
                    .quantity(item.quantity())
                    .del(false)
                    .build();

            orderItemRepository.save(itemEntity);

            itemsAmount = itemsAmount.add(amount);

            OrderItemResponse itemInfo = new OrderItemResponse(
                    productInfo,
                    item.quantity(),
                    amount
            );
            itemList.add(itemInfo);
        }

        // 최종 금액 업데이트 (이미 영속화된 엔티티이므로 setter 사용하여 변경 감지)
        orderEntity.setItemsAmount(itemsAmount);
//        orderEntity.setTotalAmount(tempAmount);

        return new OrderCartResponse(
                itemList,
                itemsAmount
//                salePrice,
//                shippingFee,
//                totalAmount
        );
    }
}