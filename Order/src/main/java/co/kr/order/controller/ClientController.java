package co.kr.order.controller;

import co.kr.order.model.dto.UserInfo;
import co.kr.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/*
 * FeignClient용 Controller
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/client/orders")
public class ClientController {

    private final OrderService orderService;

    /*
     * 주문 상태(OrderStatus) 수정 요청 (POST)
     * @param orderCode: 주문 코드
     * @param status: 결제 상태
     */
    @PostMapping("/{orderCode}/status")
    public void setStatus (
            @PathVariable("orderCode") String orderCode,
            @RequestParam String status
    ) {
        orderService.updateOrderStatus(orderCode, status);
    }

    /*
     * 주문 인덱스(OrderIdx) 정보 요청 (GET)
     * @param orderCode: 주문 코드
     */
    @GetMapping("/{orderCode}/idx")
    public Long getOrderIdx(
            @PathVariable("orderCode") String orderCode
    ) {
        return orderService.findOrderIdx(orderCode);
    }

    @PostMapping("/success/{orderCode}")
    void successPayment(
            @PathVariable String orderCode,
            @RequestBody UserInfo userInfo
    ) {
        orderService.orderSuccess(orderCode, userInfo);
    }

    @PostMapping("/fail/{orderCode}")
    void failPayment(
            @PathVariable String orderCode
    ) {
        orderService.orderFail(orderCode);
    }
}