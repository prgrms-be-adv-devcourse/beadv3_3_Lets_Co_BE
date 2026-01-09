package co.kr.product.seller.model.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long productsCode,
        String name,
        String summary,
        BigDecimal price,
        BigDecimal salePrice,
        Long viewCount
) {

}
