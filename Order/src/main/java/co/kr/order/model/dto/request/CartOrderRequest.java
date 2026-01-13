package co.kr.order.model.dto.request;

import java.util.List;

// 카트로 주문할 때 사용할 요청 body
public record CartOrderRequest (
    List<OrderRequest> orderList  // productIdx, optionIdx, quantity
) {}
