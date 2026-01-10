package co.kr.order.domain.mapper;


import co.kr.order.domain.model.dto.CartInfo;
import co.kr.order.domain.model.dto.ProductInfo;

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