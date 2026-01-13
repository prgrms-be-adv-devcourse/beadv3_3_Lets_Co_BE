package co.kr.order.model.dto.response;

import java.math.BigDecimal;

/**
 * @param orderItem : 주문 상품 정보
 * @param itemsAmount : 상품 가격 (가격 * 개수)
 * 직접 주문 (단일 상품 주문) 응답 dto
 */
public record OrderDirectResponse(
        OrderItemResponse orderItem,
        BigDecimal itemsAmount
//        BigDecimal discountAmount,
//        BigDecimal shippingFee,
//        BigDecimal totalAmount
) {}
