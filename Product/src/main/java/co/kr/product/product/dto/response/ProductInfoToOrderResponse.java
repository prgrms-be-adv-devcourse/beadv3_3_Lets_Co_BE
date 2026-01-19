package co.kr.product.product.dto.response;

import java.math.BigDecimal;

public record ProductInfoToOrderResponse(
        Long productIdx,
        Long optionIdx,
        String productName,
//        String imageUrl,
        String optionContent,
        BigDecimal price,
//        BigDecimal salePrice,

        Integer stock
) {
}
