package co.kr.product.product.model.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ProductRes(
        String productsCode,
        String name,
        BigDecimal price,
        BigDecimal salePrice,
        Long viewCount,
        String status,
        List<String> category,
        String imageUrl
) {

}
