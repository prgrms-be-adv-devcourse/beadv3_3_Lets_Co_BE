package co.kr.order.service;

import co.kr.order.model.dto.CartOrderRequest;
import co.kr.order.model.dto.OrderItemInfo;
import co.kr.order.model.dto.OrderRequest;

import java.util.List;

public interface OrderService {

    OrderItemInfo directOrder(String token, OrderRequest orderRequest);
    List<OrderItemInfo> cartOrder(String token, CartOrderRequest cartOrderRequest);
}
