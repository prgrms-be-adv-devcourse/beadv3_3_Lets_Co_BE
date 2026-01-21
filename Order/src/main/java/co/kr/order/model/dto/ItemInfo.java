package co.kr.order.model.dto;

import java.math.BigDecimal;

public record ItemInfo (
        Long productIdx,
        Long optionIdx,
        String productName,
//        String imageUrl,
        String optionName,
        BigDecimal price
) {}
