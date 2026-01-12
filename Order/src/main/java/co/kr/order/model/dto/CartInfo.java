package co.kr.order.model.dto;

import java.util.List;

public record CartInfo(
        List<CartItemInfo> productList
) {}

