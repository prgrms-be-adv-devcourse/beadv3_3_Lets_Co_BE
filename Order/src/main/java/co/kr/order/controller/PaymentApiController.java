package co.kr.order.controller;

import co.kr.order.model.dto.response.PaymentResponse;
import co.kr.order.model.dto.request.PaymentRequest;
import co.kr.order.model.dto.response.BaseResponse;
import co.kr.order.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentApiController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<BaseResponse<PaymentResponse>> processPayment(
            @RequestHeader("Authorization") String token,
            @RequestBody PaymentRequest request
    ) {
        PaymentResponse description = paymentService.process(token, request);
        BaseResponse<PaymentResponse> response = new BaseResponse<>("ok", description);

        return ResponseEntity.ok(response);
    }

}
