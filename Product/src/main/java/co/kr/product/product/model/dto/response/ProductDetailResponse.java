package co.kr.product.product.model.dto.response;

import co.kr.product.product.model.vo.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailResponse(
        Long productsIDX,
        String productsCode,
        String name,
        String description,
        BigDecimal price,
        BigDecimal salePrice,
        Long viewCount,
        Integer stock,
        ProductStatus status,
        List<ProductOptionsResponse> options,
        List<ProductImageResponse> images

) {
}
