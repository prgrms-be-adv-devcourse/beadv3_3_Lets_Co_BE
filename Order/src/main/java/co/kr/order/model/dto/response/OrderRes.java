package co.kr.order.model.dto.response;

import java.math.BigDecimal;
import java.util.List;

/*
 * return 할 주문 정보
 * @param ordersCode: 주문 코드
 * @param orderItemList: 주문한 제품 정보
 * @param itemsAmount: 최종 결제 금액
 */
public record OrderRes(
        String ordersCode,
        List<OrderItemRes> orderItemList,
        BigDecimal itemsAmount
) {}
