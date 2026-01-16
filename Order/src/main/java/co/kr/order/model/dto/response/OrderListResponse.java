package co.kr.order.model.dto.response;

import java.math.BigDecimal;
import java.util.List;

/*
 * @param itemList : 상품 리스트 정보 (OrderItemResponse)
 * @param itemsAmount : 상품 리스트 총 가격 (단일상품 * 개수) * cart에 담긴 상품 개수
 */
public record OrderListResponse(
        List<OrderItemResponse> itemList,
        BigDecimal itemsAmount
//    BigDecimal discountAmount,
//    BigDecimal shippingFee,
//    BigDecimal totalAmount
) {}
