package co.kr.product.product.model.dto.response;

import co.kr.product.product.model.vo.ProductStatus;
import co.kr.product.review.model.dto.response.ReviewResponse;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailRes(
        String productsCode,
        String name,
        String description,
        BigDecimal price,
        BigDecimal salePrice,
        Long viewCount,
        Integer stock,
        ProductStatus status,
        List<ProductOptionsRes> options,
        List<CategoryInfoRes> category,
        List<CategoryInfoRes> ip,
        List<ImageInfoRes> images,
        List<ReviewResponse> reviews

) {
}
