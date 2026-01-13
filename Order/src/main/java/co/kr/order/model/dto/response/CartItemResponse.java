package co.kr.order.model.dto.response;

import co.kr.order.model.dto.ProductInfo;

import java.math.BigDecimal;

public record CartItemResponse(
        ProductInfo product,
        Integer quantity,
        BigDecimal amount
) {}
