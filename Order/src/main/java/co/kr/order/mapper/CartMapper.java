package co.kr.order.mapper;


import co.kr.order.model.dto.CartDetails;
import co.kr.order.model.entity.OrderEntity;
import co.kr.order.model.entity.OrderItemEntity;

public class CartMapper {

    public static CartDetails toDetails(OrderItemEntity itemEntity) {

        OrderEntity order = itemEntity.getOrder();

        return new CartDetails(
                itemEntity.getId(),
                "temp",
                itemEntity.getCount(),
                itemEntity.getPrice(),
                order.getTotalAmount()
        );
    }
}
