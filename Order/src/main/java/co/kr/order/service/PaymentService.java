package co.kr.order.service;

import co.kr.order.model.dto.request.PaymentRequest;
import co.kr.order.model.dto.request.PaymentTossConfirmRequest;
import co.kr.order.model.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse process(Long userIdx, PaymentRequest request);

    PaymentResponse pay(Long userIdx, PaymentRequest request);

    PaymentResponse refund(Long userIdx, String orderCode);

    PaymentResponse confirmTossPayment(Long userIdx, PaymentTossConfirmRequest request);
}