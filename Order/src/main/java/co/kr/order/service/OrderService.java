package co.kr.order.service;

import co.kr.order.model.dto.request.OrderReq;
import co.kr.order.model.dto.response.OrderRes;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    Page<OrderRes> findOrderList(Long userIdx, Pageable pageable);
    OrderRes findOrder(Long userIdx, String orderCode);

    /**
     * 주문 완료 처리
     * - 주문 상태를 COMPLETED로 변경
     * - 정산 생성
     *
     * @param orderId 주문 ID
     */
    void completeOrder(Long orderId);

    /**
     * 주문 상태 변경 (Payment에서 콜백)
     */
    void updateOrderStatus(String orderCode, String status);

    OrderRes createOrder(Long userIdx, @Valid OrderReq request);
}
