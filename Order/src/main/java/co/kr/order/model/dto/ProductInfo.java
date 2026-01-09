package co.kr.order.model.dto;

import java.math.BigDecimal;

public record ProductInfo(
        Long productId,
        String productName,
//        String imageUrl,
        String optionContent,
        BigDecimal price,
        Integer count
) {}
