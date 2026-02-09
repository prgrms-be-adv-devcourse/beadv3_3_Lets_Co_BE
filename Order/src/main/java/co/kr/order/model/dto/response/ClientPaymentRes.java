package co.kr.order.model.dto.response;

import co.kr.order.model.vo.PaymentStatus;
import co.kr.order.model.vo.PaymentType;

import java.math.BigDecimal;

public record ClientPaymentRes(
        Long paymentIdx,
        PaymentStatus status,
        PaymentType type,
        BigDecimal amount,
        Long cardIdx,
        String paymentKey
) {}
