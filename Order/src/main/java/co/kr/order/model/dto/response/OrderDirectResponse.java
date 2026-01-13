package co.kr.order.model.dto.response;

import java.math.BigDecimal;

public record OrderDirectResponse(
    OrderItemResponse orderItem,
    BigDecimal itemsAmount
//    BigDecimal discountAmount,
//    BigDecimal shippingFee,
//    BigDecimal totalAmount
) {}
