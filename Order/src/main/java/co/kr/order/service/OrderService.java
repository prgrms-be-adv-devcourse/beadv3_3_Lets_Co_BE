package co.kr.order.service;

import co.kr.order.model.dto.OrderItemInfo;
import co.kr.order.model.dto.OrderRequest;

public interface OrderService {

    OrderItemInfo directOrder(String token, OrderRequest orderRequest);
}
