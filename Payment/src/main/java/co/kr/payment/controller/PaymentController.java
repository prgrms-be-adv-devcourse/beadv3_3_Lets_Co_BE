package co.kr.payment.controller;

import co.kr.payment.controller.swagger.payment.PaymentChargeDocs;
import co.kr.payment.controller.swagger.payment.PaymentProcessDocs;
import co.kr.payment.controller.swagger.payment.PaymentRefundDocs;
import co.kr.payment.model.dto.request.ChargeReq;
import co.kr.payment.model.dto.request.PaymentReq;
import co.kr.payment.model.dto.request.RefundReq;
import co.kr.payment.model.dto.response.PaymentResponse;
import co.kr.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
@Tag(name = "Payment", description = "결제 API")
public class PaymentController {

    private final PaymentService paymentService;

    @PaymentProcessDocs
    @PostMapping("/process")
    public ResponseEntity<String> processPayment(
            @RequestHeader("X-USERS-IDX") Long userIdx,
            @RequestBody PaymentReq request
    ) {
        paymentService.process(userIdx, request);

        return ResponseEntity.ok("주문이 완료되었습니다.");
    }

    @PaymentRefundDocs
    @PostMapping("/refund/{orderCode}")
    public ResponseEntity<PaymentResponse> refund(
            @RequestHeader("X-USERS-IDX") Long userIdx,
            @PathVariable String orderCode
    ) {
        RefundReq refundReq = new RefundReq(userIdx, orderCode);
        return ResponseEntity.ok(paymentService.refund(refundReq));
    }

    @PaymentChargeDocs
    @PostMapping("/charge")
    public ResponseEntity<PaymentResponse> charge(
            @RequestHeader("X-USERS-IDX") Long userIdx,
            @RequestBody ChargeReq request
    ) {
        return ResponseEntity.ok(paymentService.charge(userIdx, request));
    }
}