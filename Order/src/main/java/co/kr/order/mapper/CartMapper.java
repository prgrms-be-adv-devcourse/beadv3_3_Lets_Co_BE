package co.kr.order.mapper;


import co.kr.order.model.dto.response.CartRes;
import co.kr.order.model.dto.response.CartItemRes;

import java.util.List;

public class CartMapper {

    // List<CartItemRespons> -> CartResponse
    public static CartRes toCartInfo(List<CartItemRes> products) {
        return new CartRes(products);
    }
}