package co.kr.user.controller;

import co.kr.user.model.dto.payment.PaymentReq;
import co.kr.user.model.dto.payment.PaymentListDTO;
import co.kr.user.service.PaymentService;
import co.kr.user.util.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/payment/history")
    public ResponseEntity<BaseResponse<List<PaymentListDTO>>> paymentHistory(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                                             @RequestBody PaymentReq paymentReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", paymentService.balanceHistory(userIdx, paymentReq)));
    }
}