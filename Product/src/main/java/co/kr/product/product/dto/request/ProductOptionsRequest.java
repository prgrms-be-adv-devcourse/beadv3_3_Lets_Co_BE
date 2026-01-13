package co.kr.product.product.dto.request;

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
