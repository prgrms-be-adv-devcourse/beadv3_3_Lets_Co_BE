package co.kr.order.mapper;


import co.kr.order.model.dto.CartInfo;
import co.kr.order.model.dto.ProductInfo;

import java.math.BigDecimal;
import java.util.List;

public class CartMapper {

    public static CartInfo toCartInfo(
            List<ProductInfo> products,
            BigDecimal totalAmount
    ) {

        return new CartInfo(products, totalAmount);
    }
}