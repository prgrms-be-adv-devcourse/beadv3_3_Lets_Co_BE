package co.kr.order.domain.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartInfo(
        List<ProductInfo> productList,
        BigDecimal totalAmount
) {}

