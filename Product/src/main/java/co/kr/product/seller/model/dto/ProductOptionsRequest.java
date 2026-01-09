package co.kr.product.seller.model.dto;

import java.math.BigDecimal;

public record ProductOptionsRequest(
        String code,
        String name,
        int sortOrder,
        BigDecimal price,
        BigDecimal salePrice,
        int stock,
        String status
        ) {
}
