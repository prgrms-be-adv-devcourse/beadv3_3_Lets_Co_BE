package co.kr.product.seller.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record UpsertProductRequest(
    String name,
    String description,
    BigDecimal price,
    BigDecimal salePrice,
    int stock,
    String status,
    List<ProductOptionsRequest> options
    ) {
}
