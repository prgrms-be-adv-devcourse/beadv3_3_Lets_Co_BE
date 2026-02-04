package co.kr.payment.controller;

import co.kr.payment.model.dto.request.ChargeRequest;
import co.kr.payment.model.dto.response.PaymentResponse;
import co.kr.payment.model.dto.request.PaymentRequest;
import co.kr.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentApiController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestHeader("X-User-Idx") Long userIdx,
            @RequestBody PaymentRequest request
    ) {
        return ResponseEntity.ok(paymentService.process(userIdx, request));
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @RequestHeader("X-User-Idx") Long userIdx,
            @RequestParam String orderCode
    ) {
        return ResponseEntity.ok(paymentService.refund(userIdx, orderCode));
    }

    @PostMapping("/charge")
    public ResponseEntity<PaymentResponse> charge(
            @RequestHeader("X-User-Idx") Long userIdx,
            @RequestBody ChargeRequest request
    ) {
        return ResponseEntity.ok(paymentService.charge(userIdx, request));
    }

    @GetMapping("/order/{ordersIdx}")
    public ResponseEntity<PaymentResponse> findByOrdersIdx(
            @PathVariable Long ordersIdx
    ) {
        return ResponseEntity.ok(paymentService.findByOrdersIdx(ordersIdx));
    }
}
