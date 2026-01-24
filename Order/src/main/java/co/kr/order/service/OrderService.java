package co.kr.order.service;

import co.kr.order.model.dto.request.ChargeRequest;
import co.kr.order.model.dto.request.OrderCartRequest;
import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse directOrder(Long userIdx, OrderDirectRequest request);
    OrderResponse cartOrder(Long userIdx, OrderCartRequest request);

    Page<OrderResponse> findOrderList(Long userIdx, Pageable pageable);
    OrderResponse findOrder(Long userIdx, String orderCode);

    String refund(Long userIdx, String orderCode);

    /**
     * 주문 완료 처리
     * - 주문 상태를 COMPLETED로 변경
     * - 정산 생성
     *
     * @param orderId 주문 ID
     */
    void completeOrder(Long orderId);

    String charge(Long userIdx, ChargeRequest request);
}
