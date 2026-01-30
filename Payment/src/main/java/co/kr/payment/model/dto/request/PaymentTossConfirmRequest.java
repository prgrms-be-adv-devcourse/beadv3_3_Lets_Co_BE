package co.kr.payment.model.dto.request;

import java.math.BigDecimal;

public record PaymentTossConfirmRequest(
        String orderCode,
        Long ordersIdx,
        String paymentKey,
        BigDecimal amount
) {}
