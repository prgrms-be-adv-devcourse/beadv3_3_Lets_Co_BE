package co.kr.order.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartDetails(
        BigDecimal totalAmount,
        List<ProductInfo> productList
) {}

