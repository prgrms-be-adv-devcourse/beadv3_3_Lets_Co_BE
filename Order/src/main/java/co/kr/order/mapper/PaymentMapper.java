package co.kr.order.mapper;

import co.kr.order.model.dto.response.PaymentResponse;
import co.kr.order.model.entity.PaymentEntity;

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
