package co.kr.order.client;

import co.kr.order.model.dto.request.PaymentRequest;
import co.kr.order.model.dto.request.PaymentTossConfirmRequest;
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

    @PostMapping("/payments/card")
    PaymentResponse cardPayment(
            @RequestHeader("X-User-Idx") Long userIdx,
            @RequestBody PaymentRequest request
    );

    @PostMapping("/payments/deposit")
    PaymentResponse depositPayment(
            @RequestHeader("X-User-Idx") Long userIdx,
            @RequestBody PaymentRequest request
    );

    @PostMapping("/payments/toss/confirm")
    PaymentResponse confirmTossPayment(
            @RequestHeader("X-User-Idx") Long userIdx,
            @RequestBody PaymentTossConfirmRequest request
    );

    @PostMapping("/payments/refund")
    PaymentResponse refundPayment(
            @RequestHeader("X-User-Idx") Long userIdx,
            @RequestParam String orderCode
    );

    @GetMapping("/payments/order/{ordersIdx}")
    PaymentResponse findByOrdersIdx(@PathVariable Long ordersIdx);
}
