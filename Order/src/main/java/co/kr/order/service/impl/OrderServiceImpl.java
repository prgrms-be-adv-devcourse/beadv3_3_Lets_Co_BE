package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.NoInputAddressDataException;
import co.kr.order.exception.NoInputCardDataException;
import co.kr.order.exception.NoInputOrderDataException;
import co.kr.order.model.dto.UserData;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.request.CartOrderRequest;
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

    /**
     * Todo
     * - 재고가 없을때 처리
     * - N+1 문제
     * - 카트/단일 주문의 중복 로직을 통합
     */

    @Transactional
    @Override
    public OrderDirectResponse directOrder(String token, OrderRequest orderRequest) {

        // todo: 유저가 카드/주소 데이터(DTO)를 body에 넣었을 때 Member-service에 데이터 전송

        // Product 서비스에 feignClient(동기통신) 으로 제품 정보 가져오고, 가격*수량 해서 총 가격 측정
        ProductInfo productInfo = productClient.getProduct(orderRequest.productIdx(), orderRequest.optionIdx());
        BigDecimal itemAmount = productInfo.price().multiply(BigDecimal.valueOf(orderRequest.quantity()));

        // Member 서비스에 동기통신 해서 userIdx, AddressIdx, CardIdx 가져옴
        UserData userData = userClient.getOrderData(token);
        validateOrderData(userData);

        // Order Table 세팅
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserIdx(userData.usersIdx());
        orderEntity.setAddressIdx(userData.addressIdx());
        orderEntity.setCardIdx(userData.cardIdx());
        orderEntity.setOrderCode(UUID.randomUUID().toString());
        orderEntity.setStatus(OrderStatus.CREATED.name());
        orderEntity.setItemsAmount(itemAmount);
//        itemEntity.setSalePrice();
//        itemEntity.setShippingFee();
        orderEntity.setTotalAmount(itemAmount);  // 일단 할인/배송비 고려 x
        orderEntity.setDel(false);
        orderRepository.save(orderEntity);

        // OrderItem Table 세팅
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrder(orderEntity);
        itemEntity.setProductIdx(productInfo.productIdx());
        itemEntity.setOptionIdx(orderRequest.optionIdx());
        itemEntity.setProductName(productInfo.productName());
        itemEntity.setOptionName(productInfo.optionContent());
        itemEntity.setPrice(productInfo.price());
        itemEntity.setQuantity(orderRequest.quantity());
        itemEntity.setDel(false);
        orderItemRepository.save(itemEntity);

        // 상품을 상세 내용
        OrderItemResponse itemInfo = new OrderItemResponse(
                productInfo.productIdx(),
                productInfo.productName(),
//                        productInfo.imageUrl(),
                productInfo.optionContent(),
                productInfo.price(),
                orderRequest.quantity()
        );

        return new OrderDirectResponse(
                itemInfo,
                itemAmount
//                salePrice,
//                shippingFee,
//                totalAmount
        );
    }

    @Transactional
    @Override
    public OrderCartResponse cartOrder(String token, CartOrderRequest cartOrderRequest) {

        // todo: 유저가 카드/주소 데이터(DTO)를 body에 넣었을 때 Member-service에 데이터 전송
        UserData userData = userClient.getOrderData(token);
        validateOrderData(userData);

        List<OrderItemResponse> itemList = new ArrayList<>();
        BigDecimal itemAmount = BigDecimal.ZERO;

        // Order Table 세팅
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserIdx(userData.usersIdx());
        orderEntity.setAddressIdx(userData.addressIdx());
        orderEntity.setCardIdx(userData.cardIdx());
        orderEntity.setOrderCode(UUID.randomUUID().toString());
        orderEntity.setStatus(OrderStatus.CREATED.name());
        orderEntity.setItemsAmount(itemAmount);
//        orderEntity.setSalePrice();
//        orderEntity.setShippingFee();
        orderEntity.setTotalAmount(itemAmount);
        orderEntity.setDel(false);
        orderRepository.save(orderEntity);

        List<OrderRequest> items = cartOrderRequest.orderList();
        for(OrderRequest item : items) {
            ProductInfo productInfo = productClient.getProduct(item.productIdx(), item.optionIdx());
            BigDecimal unitAmount = productInfo.price().multiply(BigDecimal.valueOf(item.quantity()));

            // OrderItem Table 세팅
            OrderItemEntity itemEntity = new OrderItemEntity();
            itemEntity.setOrder(orderEntity);
            itemEntity.setProductIdx(productInfo.productIdx());
            itemEntity.setOptionIdx(item.optionIdx());
            itemEntity.setProductName(productInfo.productName());
            itemEntity.setOptionName(productInfo.optionContent());
            itemEntity.setPrice(productInfo.price());
//        itemEntity.setSalePrice(productInfo.salePrice());
            itemEntity.setQuantity(item.quantity());
            itemEntity.setDel(false);
            orderItemRepository.save(itemEntity);

            itemAmount = itemAmount.add(unitAmount);

            OrderItemResponse itemInfo = new OrderItemResponse(
                    productInfo.productIdx(),
                    productInfo.productName(),
//                        productInfo.imageUrl(),
                    productInfo.optionContent(),
                    productInfo.price(),
                    item.quantity()
            );
            itemList.add(itemInfo);
        }
        orderEntity.setItemsAmount(itemAmount);
//        orderEntity.setTotalAmount(tempAmount);

        return new OrderCartResponse(
                itemList,
                itemAmount
//                salePrice,
//                shippingFee,
//                totalAmount
        );
    }

    private void validateOrderData(UserData userData) {
        if (userData.addressIdx() == null && userData.cardIdx() == null) {
            throw new NoInputOrderDataException(ErrorCode.NO_INPUT_ORDER_DATA);
        }
        else if (userData.addressIdx() == null) {
            throw new NoInputAddressDataException(ErrorCode.NO_INPUT_ADDRESS_DATA);
        }
        else if (userData.cardIdx() == null) {
            throw new NoInputCardDataException(ErrorCode.NO_INPUT_CARD_DATA);
        }
    }
}
