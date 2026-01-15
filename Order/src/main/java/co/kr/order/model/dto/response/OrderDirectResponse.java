package co.kr.order.model.dto.response;

/*
 * @param item : 주문 상품 정보
 * 직접 주문 (단일 상품 주문) 응답 dto
 */
public record OrderDirectResponse(
        OrderItemResponse item
//        BigDecimal discountAmount,
//        BigDecimal shippingFee,
//        BigDecimal totalAmount
) {}
