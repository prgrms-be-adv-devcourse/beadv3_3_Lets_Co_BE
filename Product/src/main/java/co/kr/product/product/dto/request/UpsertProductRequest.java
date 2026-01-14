package co.kr.product.product.dto.request;

import co.kr.product.product.dto.vo.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public record UpsertProductRequest(
        Long productsIdx,
        String name,
        String description,
        BigDecimal price,
        BigDecimal salePrice,
        int stock,
        ProductStatus status,
        List<ProductOptionsRequest> options,
        List<ProductImagesRequest> images
        ) {
}
