package co.kr.payment.model.dto.response;

import co.kr.payment.model.vo.PaymentStatus;
import co.kr.payment.model.vo.PaymentType;

import java.math.BigDecimal;

public record PaymentResponse(
        Long paymentIdx,
        PaymentStatus status,
        PaymentType type,
        BigDecimal amount,
        Long cardIdx,
        String paymentKey
) {}
