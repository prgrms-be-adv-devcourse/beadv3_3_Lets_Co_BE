package co.kr.user.controller;

import co.kr.user.model.DTO.Payment.PaymentDateOptionReq;
import co.kr.user.model.DTO.Payment.PaymentListDTO;
import co.kr.user.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class BalanceController {
    private final BalanceService balanceService;

    @PostMapping("/balance")
    public ResponseEntity<BigDecimal> balance(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(balanceService.balance(userIdx));
    }

    @PostMapping("/balance/history")
    public ResponseEntity<List<PaymentListDTO>> balanceHistory(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(balanceService.balanceHistory(userIdx));
    }

    @PostMapping("/balance/history/option")
    public ResponseEntity<List<PaymentListDTO>> balanceHistoryOption(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                                     @RequestBody PaymentDateOptionReq paymentDateOptionReq) {
        return ResponseEntity.ok(balanceService.balanceHistoryOption(userIdx, paymentDateOptionReq));
    }
}