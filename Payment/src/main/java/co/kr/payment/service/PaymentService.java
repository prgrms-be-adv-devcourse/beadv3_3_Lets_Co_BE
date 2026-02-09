package co.kr.payment.service;

import co.kr.payment.model.dto.request.ChargeReq;
import co.kr.payment.model.dto.request.PaymentReq;
import co.kr.payment.model.dto.request.RefundReq;
import co.kr.payment.model.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse process(PaymentReq request);

    PaymentResponse refund(RefundReq refundRequest);

    PaymentResponse charge(ChargeReq request);

    PaymentResponse findByOrdersIdx(Long ordersIdx);
}
