package co.kr.order.controller;

import co.kr.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client/orders")
public class ClientController {

    private final OrderService orderService;

    @PostMapping("/{orderCode}/status")
    public void setStatus (
            @PathVariable("orderCode") String orderCode,
            @RequestParam String status
    ) {
        orderService.updateOrderStatus(orderCode, status);
    }

    @GetMapping("/{orderCode}/idx")
    public Long getOrderIdx(
            @PathVariable("orderCode") String orderCode
    ) {
        return orderService.findOrderIdx(orderCode);
    }
}