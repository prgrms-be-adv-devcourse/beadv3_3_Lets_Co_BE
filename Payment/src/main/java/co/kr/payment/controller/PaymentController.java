package co.kr.payment.controller;

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

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refund(
            @RequestHeader("X-USERS-IDX") Long userIdx,
            @RequestBody RefundReq request
    ) {
        RefundReq refundReq = new RefundReq(userIdx, request.orderCode());
        return ResponseEntity.ok(paymentService.refund(refundReq));
    }
}