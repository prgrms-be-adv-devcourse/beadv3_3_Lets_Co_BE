package co.kr.order.model.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productIdx,
        String productName,
//        String imageUrl,
        String optionContent,
        BigDecimal price,
        Integer quantity
) {}
