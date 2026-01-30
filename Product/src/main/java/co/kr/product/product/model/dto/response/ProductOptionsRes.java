package co.kr.product.product.model.dto.response;

import java.math.BigDecimal;

public record ProductOptionsRes(
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
