package co.kr.order.model.dto.response;

import java.math.BigDecimal;

public record ClientProductRes(
        Long productIdx,
        Long optionIdx,
        String productCode,
        String optionCode,
        Long sellerIdx,
        String productName,
//        String imageUrl,
        String optionContent,
        BigDecimal price,
        Integer stock
) {}
