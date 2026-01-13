package co.kr.order.model.dto.response;

import java.math.BigDecimal;

// 일단 이렇게 하는데 나중에 구조 바꿀 예정 (ProductInfo + price/quantity 로)
public record OrderItemResponse(
        Long productIdx,
        String productName,
//        String imageUrl,
        String optionContent,
        BigDecimal price,
        Integer quantity
) {}
