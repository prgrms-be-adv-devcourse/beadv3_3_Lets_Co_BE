package co.kr.order.service;

import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.request.UserDataRequest;
import co.kr.order.model.dto.response.OrderCartResponse;
import co.kr.order.model.dto.response.OrderDirectResponse;

public interface OrderService {

    OrderDirectResponse directOrder(Long userIdx, OrderDirectRequest request);
    OrderCartResponse cartOrder(Long userIdx, UserDataRequest request);

    /**
     * 주문 완료 처리
     * - 주문 상태를 COMPLETED로 변경
     * - 정산 생성
     *
     * @param orderId 주문 ID
     */
    void completeOrder(Long orderId);
}
