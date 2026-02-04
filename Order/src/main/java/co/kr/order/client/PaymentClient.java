package co.kr.order.client;

import co.kr.order.model.dto.request.PaymentRequest;
import co.kr.order.model.dto.response.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "Payment")
public interface PaymentClient {

    @PostMapping("/payments/process")
    PaymentResponse processPayment(
            @RequestHeader("X-User-Idx") Long userIdx,
            @RequestBody PaymentRequest request
    );

    @PostMapping("/payments/refund")
    PaymentResponse refundPayment(
            @RequestHeader("X-User-Idx") Long userIdx,
            @RequestParam String orderCode
    );

    @GetMapping("/payments/order/{ordersIdx}")
    PaymentResponse findByOrdersIdx(@PathVariable Long ordersIdx);
}
