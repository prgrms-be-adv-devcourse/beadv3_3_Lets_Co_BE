package co.kr.order.service;

import co.kr.order.model.dto.request.OrderCartRequest;
import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.response.OrderCartResponse;
import co.kr.order.model.dto.response.OrderDirectResponse;

public interface OrderService {

    OrderDirectResponse directOrder(String token, OrderDirectRequest request);
    OrderCartResponse cartOrder(String token, OrderCartRequest request);
}
