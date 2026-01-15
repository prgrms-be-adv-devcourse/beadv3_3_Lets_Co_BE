package co.kr.order.model.dto.request;

import co.kr.order.model.vo.PaymentType;

public record PaymentRequest(
        String orderCode,
        PaymentType paymentType
) {}
