package co.kr.order.mapper;


import co.kr.order.client.ProductClient;
import co.kr.order.model.dto.CartDetails;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.entity.CartEntity;

import java.util.List;

public class CartMapper {

    private ProductClient productClient;

    public static CartDetails toDetails(CartEntity cart, ProductInfo product) {
        return new CartDetails(
                cart.getPrice(),
                List.of(product)
        );
    }
}
