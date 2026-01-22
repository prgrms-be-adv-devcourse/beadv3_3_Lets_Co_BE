package co.kr.order.model.dto.request;

import java.math.BigDecimal;

public record PaymentTossConfirmRequest(
        String orderCode,
        String paymentKey,
        BigDecimal amount
) {}
