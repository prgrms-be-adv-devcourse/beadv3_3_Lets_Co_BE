package co.kr.order.service;

import co.kr.order.model.dto.response.PaymentResponse;
import co.kr.order.model.dto.request.PaymentRequest;

public interface PaymentService {
    // process로 잡고 결제 수단에 따라 내부에서 분기처리
    PaymentResponse process(String token, PaymentRequest request);
}
