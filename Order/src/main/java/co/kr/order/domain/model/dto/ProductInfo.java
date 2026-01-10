package co.kr.order.domain.model.dto;

import java.math.BigDecimal;

public record ProductInfo(
        Long productId,
        String productName,
//        String imageUrl,
        String optionContent,
        BigDecimal price,
        Integer quantity,
        BigDecimal totalPrice
) {
    public ProductInfo toProductInfo(int quantity, BigDecimal totalPrice) {
        return new ProductInfo(this.productId, this.productName, this.optionContent, this.price, quantity, totalPrice);
    }
}
