package co.kr.order.service;

import co.kr.order.model.dto.UserInfo;
import co.kr.order.model.dto.request.OrderReq;
import co.kr.order.model.dto.response.OrderRes;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderRes createOrder(Long userIdx, @Valid OrderReq request);
    Page<OrderRes> findOrderList(Long userIdx, Pageable pageable);
    OrderRes findOrder(Long userIdx, String orderCode);

    void updateOrderStatus(String orderCode, String status);
    Long findOrderIdx(String orderCode);

    void orderSuccess(String orderCode, Long paymentIdx, UserInfo userInfo);
    void orderFail(String orderCode);
}
