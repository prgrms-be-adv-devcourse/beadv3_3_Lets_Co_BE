package co.kr.order.model.dto.response;

import java.util.List;

// 카트에 담긴 상품 정보 리스트 (CartItemResponse)
public record CartRes(
        List<CartItemRes> cartItemList
) {}

