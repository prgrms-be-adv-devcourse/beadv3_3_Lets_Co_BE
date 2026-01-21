package co.kr.order.model.dto.response;

import co.kr.order.model.dto.ItemInfo;

import java.math.BigDecimal;

/*
 * @param item : 제품 정보
 * @param quantity : 카트에 담긴 제품의 개수
 * @param amount : 가격 * 개수
 */
public record CartItemResponse(
        ItemInfo product,
        Integer quantity,
        BigDecimal amount
) {}
