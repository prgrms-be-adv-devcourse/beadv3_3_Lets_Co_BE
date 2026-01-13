package co.kr.order.model.dto.response;

import java.util.List;

public record CartResponse(
        List<CartItemResponse> cartItemList
) {}

