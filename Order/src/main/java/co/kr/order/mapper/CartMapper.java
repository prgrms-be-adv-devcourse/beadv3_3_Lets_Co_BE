package co.kr.order.mapper;


import co.kr.order.model.dto.CartInfo;
import co.kr.order.model.dto.CartItemInfo;

import java.util.List;

public class CartMapper {

    public static CartInfo toCartInfo(List<CartItemInfo> products) {
        return new CartInfo(products);
    }
}