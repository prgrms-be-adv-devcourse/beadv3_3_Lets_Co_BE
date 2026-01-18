package co.kr.user.service;

import co.kr.user.model.DTO.Payment.PaymentDateOptionReq;
import co.kr.user.model.DTO.Payment.PaymentListDTO;

import java.math.BigDecimal;
import java.util.List;

public interface BalanceServiceImpl {
    BigDecimal balance(Long userIdx);

    List<PaymentListDTO> balanceHistory(Long userIdx);

    List<PaymentListDTO> balanceHistoryOption(Long userIdx, PaymentDateOptionReq paymentDateOptionReq);
}