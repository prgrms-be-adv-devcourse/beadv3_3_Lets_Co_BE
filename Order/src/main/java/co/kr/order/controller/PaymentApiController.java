package co.kr.order.controller;

import co.kr.order.model.dto.response.PaymentResponse;
import co.kr.order.model.dto.request.PaymentRequest;
import co.kr.order.model.dto.request.PaymentTossConfirmRequest;
import co.kr.order.model.dto.response.BaseResponse;
import co.kr.order.service.PaymentService;
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

}
