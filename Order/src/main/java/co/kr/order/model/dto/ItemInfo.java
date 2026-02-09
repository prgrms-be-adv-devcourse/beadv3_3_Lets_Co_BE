package co.kr.order.model.dto;

import java.math.BigDecimal;

public record ItemInfo (
        String productCode,
        String optionCode,
        String productName,
        String optionContent,
        BigDecimal price
) {}
