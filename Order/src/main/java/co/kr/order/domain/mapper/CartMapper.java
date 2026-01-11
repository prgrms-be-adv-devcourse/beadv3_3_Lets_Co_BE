package co.kr.order.domain.mapper;


import co.kr.order.domain.model.dto.CartInfo;
import co.kr.order.domain.model.dto.CartItemInfo;

import java.util.List;

public class CartMapper {

    public static CartInfo toCartInfo(List<CartItemInfo> products) {
        return new CartInfo(products);
    }
}