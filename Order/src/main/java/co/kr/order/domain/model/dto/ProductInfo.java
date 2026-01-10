package co.kr.order.domain.model.dto;

import java.math.BigDecimal;

public record ProductInfo(
        Long productId,
        String productName,
//        String imageUrl,
        String optionContent,
        BigDecimal price,
        Integer quantity
) {
    public ProductInfo withQuantity(int count) {
        return new ProductInfo(this.productId, this.productName, this.optionContent, this.price, count);
    }
}
