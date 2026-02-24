package co.kr.order.model.dto.response;

import co.kr.order.model.dto.ItemInfo;

import java.math.BigDecimal;

/*
 * 주문 한 상품 정보
 * @param product: 제품 정보
 * @param quantity: 개수
 * @param amount: 주문 가격 (단가 * 개수)
 */
public record OrderItemRes(
        ItemInfo product,
        Integer quantity,
        BigDecimal amount
//        BigDecimal discountAmount,
//        BigDecimal shippingFee
) {}


