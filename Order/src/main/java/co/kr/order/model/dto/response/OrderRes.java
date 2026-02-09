package co.kr.order.model.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record OrderRes(
        String ordersCode,
        List<OrderItemRes> orderItemList,
        BigDecimal itemsAmount
) {}
