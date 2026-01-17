package co.kr.order.service;

import co.kr.order.model.dto.request.OrderCartRequest;
import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.response.OrderListResponse;
import co.kr.order.model.dto.response.OrderResponse;

public interface OrderService {

    OrderResponse directOrder(Long userIdx, OrderDirectRequest request);
    OrderListResponse cartOrder(Long userIdx, OrderCartRequest request);

    OrderListResponse findOrderList(Long userIdx);
    OrderResponse findOrder(Long userIdx, String orderCode);

    String refund(Long userIdx, String orderCode);
}
