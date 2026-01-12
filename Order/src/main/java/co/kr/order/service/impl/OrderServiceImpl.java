package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.NoInputAddressDataException;
import co.kr.order.exception.NoInputCardDataException;
import co.kr.order.exception.NoInputOrderDataException;
import co.kr.order.model.dto.GetOrderData;
import co.kr.order.model.dto.OrderItemInfo;
import co.kr.order.model.dto.OrderRequest;
import co.kr.order.model.dto.ProductInfo;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;

    private final ProductClient productClient;
    private final UserClient userClient;

    @Transactional
    @Override
    public OrderItemInfo directOrder(String token, OrderRequest orderRequest) {

        ProductInfo productInfo = productClient.getProduct(orderRequest.productIdx(), orderRequest.optionIdx());
        BigDecimal itemAmount = productInfo.price().multiply(BigDecimal.valueOf(orderRequest.quantity()));

        GetOrderData orderData = userClient.getOrderData(token);
        validateOrderData(orderData);

        // todo: 유저가 카드/주소 데이터(DTO)를 body에 넣었을 때 Member-service에 데이터 전송

        // Order Table 세팅
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserIdx(orderData.usersIdx());
        orderEntity.setAddressIdx(orderData.addressIdx());
        orderEntity.setCardIdx(orderData.cardIdx());
        orderEntity.setOrderCode(UUID.randomUUID().toString());
        orderEntity.setStatus(OrderStatus.CREATED.name());
        orderEntity.setItemsAmount(itemAmount);
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
//        itemEntity.setSalePrice(productInfo.salePrice());
        itemEntity.setQuantity(orderRequest.quantity());
        itemEntity.setDel(false);
        orderItemRepository.save(itemEntity);

        return new OrderItemInfo(
                productInfo.productIdx(),
                productInfo.productName(),
//                productInfo.imageUrl(),
                productInfo.optionContent(),
                productInfo.price(),
                orderRequest.quantity()
        );
    }

    private void validateOrderData(GetOrderData orderData) {
        if (orderData.addressIdx() == null && orderData.cardIdx() == null) {
            throw new NoInputOrderDataException(ErrorCode.NO_INPUT_ORDER_DATA);
        }
        else if (orderData.addressIdx() == null) {
            throw new NoInputAddressDataException(ErrorCode.NO_INPUT_ADDRESS_DATA);
        }
        else if (orderData.cardIdx() == null) {
            throw new NoInputCardDataException(ErrorCode.NO_INPUT_CARD_DATA);
        }
    }
}
