package co.kr.order.domain.model.dto;

import java.math.BigDecimal;

public record CartItemInfo(
        Long productIdx,
        String productName,
//        String imageUrl,
        String optionContent,
        BigDecimal price,
        Integer quantity,
        BigDecimal totalPrice
) {}
