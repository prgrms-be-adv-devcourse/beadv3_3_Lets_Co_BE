package co.kr.order.service;

import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.request.UserDataRequest;
import co.kr.order.model.dto.response.OrderCartResponse;
import co.kr.order.model.dto.response.OrderDirectResponse;

public interface OrderService {

    OrderDirectResponse directOrder(Long userIdx, OrderDirectRequest request);
    OrderCartResponse cartOrder(Long userIdx, UserDataRequest request);
}
