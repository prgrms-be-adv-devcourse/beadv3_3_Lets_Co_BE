package co.kr.order.model.dto.request;

import co.kr.order.model.vo.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ChargeRequest (
        @NotNull(message = "충전 금액은 필수입니다.")
        @Positive(message = "충전 금액은 0보다 커야 합니다.")
        BigDecimal amount,

        @NotNull(message = "결제 수단은 필수입니다.")
        PaymentType paymentType
) {}
