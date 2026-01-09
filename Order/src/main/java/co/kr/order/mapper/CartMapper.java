package co.kr.order.mapper;


import co.kr.order.model.dto.CartInfo;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.entity.CartEntity;

import java.math.BigDecimal;
import java.util.List;

public class CartMapper {

    public static CartInfo toInfo(List<CartEntity> entities, List<ProductInfo> products) {
        BigDecimal totalAmount = entities.stream()
                .map(CartEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartInfo(
                products,
                entities.size(),
                totalAmount
        );
    }
}