package co.kr.product.product.dto.response;

import java.math.BigDecimal;

public record ProductOptionsResponse(
        Long optionGroupIdx,
        String code,
        String name,
        int sortOrder,
        BigDecimal price,
        BigDecimal salePrice,
        int stock,
        String status
) {
}
