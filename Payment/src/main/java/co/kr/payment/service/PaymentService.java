package co.kr.payment.service;

import co.kr.payment.model.dto.request.ChargeRequest;
import co.kr.payment.model.dto.request.PaymentRequest;
import co.kr.payment.model.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse process(Long userIdx, PaymentRequest request);

    PaymentResponse refund(Long userIdx, String orderCode);

    PaymentResponse charge(Long userIdx, ChargeRequest request);

    PaymentResponse findByOrdersIdx(Long ordersIdx);
}
