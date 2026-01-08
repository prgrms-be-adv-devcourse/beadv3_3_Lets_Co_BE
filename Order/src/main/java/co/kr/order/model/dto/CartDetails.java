package co.kr.order.model.dto;

import java.math.BigDecimal;

public record CartDetails(
        Long itemId,
        String productName,  // product table
        Integer count,
        BigDecimal price,
        BigDecimal amount
) {}

