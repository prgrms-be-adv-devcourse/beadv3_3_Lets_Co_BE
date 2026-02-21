package co.kr.payment.controller;

import co.kr.payment.model.dto.request.ChargeReq;
import co.kr.payment.model.dto.request.PaymentReq;
import co.kr.payment.model.dto.request.RefundReq;
import co.kr.payment.model.dto.response.PaymentResponse;
import co.kr.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<String> processPayment(
            @RequestHeader("X-USERS-IDX") Long userIdx,
            @RequestBody PaymentReq request
    ) {
        paymentService.process(userIdx, request);

        return ResponseEntity.ok("주문이 완료되었습니다.");
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refund(
            @RequestHeader("X-USERS-IDX") Long userIdx,
            @RequestBody RefundReq request
    ) {
        RefundReq refundReq = new RefundReq(userIdx, request.orderCode());
        return ResponseEntity.ok(paymentService.refund(refundReq));
    }

    @PostMapping("/charge")
    public ResponseEntity<PaymentResponse> charge(
            @RequestHeader("X-USERS-IDX") Long userIdx,
            @RequestBody ChargeReq request
    ) {
        return ResponseEntity.ok(paymentService.charge(userIdx, request));
    }
}