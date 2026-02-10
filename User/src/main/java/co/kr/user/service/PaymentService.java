package co.kr.user.service;

import co.kr.user.model.dto.payment.PaymentReq;
import co.kr.user.model.dto.payment.PaymentListDTO;

import java.util.List;

public interface PaymentService {
    List<PaymentListDTO> balanceHistory(Long userIdx, PaymentReq paymentReq);
}