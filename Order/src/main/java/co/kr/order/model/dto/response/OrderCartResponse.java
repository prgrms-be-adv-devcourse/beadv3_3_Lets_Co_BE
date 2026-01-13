package co.kr.order.model.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record OrderCartResponse(
        List<OrderItemResponse> orderItemList,
        BigDecimal itemsAmount
//    BigDecimal discountAmount,
//    BigDecimal shippingFee,
//        BigDecimal totalAmount
) {}
