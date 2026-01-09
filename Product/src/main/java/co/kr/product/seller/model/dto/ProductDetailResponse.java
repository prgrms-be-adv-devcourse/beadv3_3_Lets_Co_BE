package co.kr.product.seller.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailResponse(
        String resultCode,

        String name,
        String description,
        BigDecimal price,
        BigDecimal salePrice,
        Long viewCount,
        int stock,
        String status,

        List<ProductOptionsResponse> options
) {
}
