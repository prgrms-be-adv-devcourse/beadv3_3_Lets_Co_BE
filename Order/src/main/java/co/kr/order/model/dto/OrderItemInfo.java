package co.kr.order.model.dto;

import java.math.BigDecimal;

public record OrderItemInfo (
        Long productIdx,
        String productName,
//        String imageUrl,
        String optionContent,
        BigDecimal price,
        Integer quantity
) {}
