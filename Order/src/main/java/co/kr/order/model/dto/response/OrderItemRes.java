package co.kr.order.model.dto.response;

import co.kr.order.model.dto.ItemInfo;

import java.math.BigDecimal;

public record OrderItemRes(
        ItemInfo product,
        Integer quantity,
        BigDecimal amount
//        BigDecimal discountAmount,
//        BigDecimal shippingFee
) {}


