package co.kr.order.service;

import co.kr.order.model.dto.request.OrderCartRequest;
import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse directOrder(Long userIdx, OrderDirectRequest request);
    OrderResponse cartOrder(Long userIdx, OrderCartRequest request);

    List<OrderResponse> findOrderList(Long userIdx);
    OrderResponse findOrder(Long userIdx, String orderCode);

    String refund(Long userIdx, String orderCode);
}
