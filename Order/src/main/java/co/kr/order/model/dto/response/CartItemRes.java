package co.kr.order.model.dto.response;

import co.kr.order.model.dto.ItemInfo;

import java.math.BigDecimal;

/*
 * return 할 장바구니 정보
 * @param product: 제품 정보
 * @param quantity: 개수
 * @param amount: 최종 금액
 */
public record CartItemRes(
        ItemInfo product,
        Integer quantity,
        BigDecimal amount
) {}
