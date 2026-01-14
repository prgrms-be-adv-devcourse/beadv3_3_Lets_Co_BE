package co.kr.order.service;

import co.kr.order.model.dto.request.CartOrderRequest;
import co.kr.order.model.dto.request.OrderRequest;
import co.kr.order.model.dto.response.OrderCartResponse;
import co.kr.order.model.dto.response.OrderDirectResponse;

public interface OrderService {

    OrderDirectResponse directOrder(String token, OrderRequest orderRequest);
    OrderCartResponse cartOrder(String token, CartOrderRequest cartOrderRequest);
}
