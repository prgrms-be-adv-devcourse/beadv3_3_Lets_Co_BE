package co.kr.product.product.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailRes(

        String resultCode,

        Long productsIDX,
        String productsCode,
        String name,
        String description,
        BigDecimal price,
        BigDecimal salePrice,
        Long viewCount,
        Integer stock,
        co.kr.product.product.dto.vo.ProductStatus status,
        List<ProductOptionsRes> options,
        List<ProductImageRes> images

) {
}
