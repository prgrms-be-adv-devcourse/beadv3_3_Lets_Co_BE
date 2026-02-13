package co.kr.payment.controller;

import co.kr.payment.model.dto.request.PaymentReq;
import co.kr.payment.model.dto.request.RefundReq;
import co.kr.payment.model.dto.response.PaymentResponse;
import co.kr.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client/payments")
public class PaymentApiController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestBody PaymentReq request
    ) {
        return ResponseEntity.ok(paymentService.process(request));
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @RequestBody RefundReq request
    ) {
        return ResponseEntity.ok(paymentService.refund(request));
    }

}
