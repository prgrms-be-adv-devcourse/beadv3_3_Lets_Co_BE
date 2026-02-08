package co.kr.order.model.dto;

import java.math.BigDecimal;

public record ItemInfo (
        String productName,
        String optionContent,
        BigDecimal price
) {}
