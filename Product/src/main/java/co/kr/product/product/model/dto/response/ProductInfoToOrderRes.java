package co.kr.product.product.model.dto.response;

import java.math.BigDecimal;

public record ProductInfoToOrderRes(
        Long productIdx,
        Long optionIdx,
        Long sellerIdx,
        String productCode,
        String optionCode,
        String productName,
//        String imageUrl,
        String optionContent,
        BigDecimal price,
//        BigDecimal salePrice,

        Integer stock

) {
}
