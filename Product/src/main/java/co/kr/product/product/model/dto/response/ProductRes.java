package co.kr.product.product.model.dto.response;

import java.math.BigDecimal;

public record ProductRes(
        Long productsIdx,
        String productsCode,
        String name,
        BigDecimal price,
        BigDecimal salePrice,
        Long viewCount
) {

}
