package co.kr.payment.mapper;

import co.kr.payment.model.dto.response.PaymentResponse;
import co.kr.payment.model.entity.PaymentEntity;

public class PaymentMapper {
    public static PaymentResponse toResponse(PaymentEntity payment) {
        return new PaymentResponse(
                payment.getPaymentIdx(),
                payment.getStatus(),
                payment.getType(),
                payment.getAmount(),
                payment.getOrdersIdx(),
                payment.getCardIdx(),
                payment.getPaymentKey()
        );
    }
}
