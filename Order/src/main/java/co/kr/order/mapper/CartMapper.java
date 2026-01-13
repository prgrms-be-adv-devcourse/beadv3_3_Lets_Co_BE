package co.kr.order.mapper;


import co.kr.order.model.dto.response.CartResponse;
import co.kr.order.model.dto.response.CartItemResponse;

import java.util.List;

public class CartMapper {

    public static CartResponse toCartInfo(List<CartItemResponse> products) {
        return new CartResponse(products);
    }
}