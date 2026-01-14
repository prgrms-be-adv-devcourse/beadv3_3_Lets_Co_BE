package co.kr.order.controller;


import co.kr.order.model.dto.BaseResponse;
import co.kr.order.model.dto.PaymentDescription;
import co.kr.order.model.dto.PaymentRequest;
import co.kr.order.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentApiController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<BaseResponse<PaymentDescription>> processPayment(
            @RequestHeader("Authorization") String token,
            @RequestBody PaymentRequest request
    ) {
        PaymentDescription description = paymentService.process(token, request);
        BaseResponse<PaymentDescription> response = new BaseResponse<>("ok", description);

        return ResponseEntity.ok(response);
    }

}
