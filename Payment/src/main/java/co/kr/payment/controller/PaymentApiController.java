package co.kr.payment.controller;

import co.kr.payment.model.dto.request.ChargeRequest;
import co.kr.payment.model.dto.response.PaymentResponse;
import co.kr.payment.model.dto.request.PaymentRequest;
import co.kr.payment.model.dto.request.PaymentTossConfirmRequest;
import co.kr.payment.model.dto.response.BaseResponse;
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
    public ResponseEntity<BaseResponse<PaymentResponse>> processPayment(
            @RequestHeader("X-User-Idx") Long userIdx,
            @RequestBody PaymentRequest request
    ) {
        PaymentResponse description = paymentService.process(userIdx, request);
        BaseResponse<PaymentResponse> response = new BaseResponse<>("ok", description);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/card")
    public ResponseEntity<BaseResponse<PaymentResponse>> cardPayment(
            @RequestHeader("X-User-Idx") Long userIdx,
            @RequestBody PaymentRequest request
    ) {
        PaymentResponse description = paymentService.pay(userIdx, request);
        BaseResponse<PaymentResponse> response = new BaseResponse<>("ok", description);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<BaseResponse<PaymentResponse>> depositPayment(
            @RequestHeader("X-User-Idx") Long userIdx,
            @RequestBody PaymentRequest request
    ) {
        PaymentResponse description = paymentService.pay(userIdx, request);
        BaseResponse<PaymentResponse> response = new BaseResponse<>("ok", description);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/toss/confirm")
    public ResponseEntity<BaseResponse<PaymentResponse>> confirmTossPayment(
            @RequestHeader("X-User-Idx") Long userIdx,
            @RequestBody PaymentTossConfirmRequest request
    ) {
        PaymentResponse description = paymentService.confirmTossPayment(userIdx, request);
        BaseResponse<PaymentResponse> response = new BaseResponse<>("ok", description);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refund")
    public ResponseEntity<BaseResponse<PaymentResponse>> refundPayment(
            @RequestHeader("X-User-Idx") Long userIdx,
            @RequestParam String orderCode
    ) {
        PaymentResponse description = paymentService.refund(userIdx, orderCode);
        BaseResponse<PaymentResponse> response = new BaseResponse<>("ok", description);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/charge")
    public ResponseEntity<BaseResponse<PaymentResponse>> charge(
            @RequestHeader("X-User-Idx") Long userIdx,
            @RequestBody ChargeRequest request
    ) {
        PaymentResponse description = paymentService.charge(userIdx, request);
        BaseResponse<PaymentResponse> response = new BaseResponse<>("ok", description);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-order/{ordersIdx}")
    public ResponseEntity<BaseResponse<PaymentResponse>> findByOrdersIdx(
            @PathVariable Long ordersIdx
    ) {
        PaymentResponse description = paymentService.findByOrdersIdx(ordersIdx);
        BaseResponse<PaymentResponse> response = new BaseResponse<>("ok", description);

        return ResponseEntity.ok(response);
    }
}
