package co.kr.order.service;

import co.kr.order.model.dto.PaymentDescription;
import co.kr.order.model.dto.PaymentRequest;

public interface PaymentService {
    // process로 잡고 결제 수단에 따라 내부에서 분기처리
    PaymentDescription process(String token, PaymentRequest request);
}
