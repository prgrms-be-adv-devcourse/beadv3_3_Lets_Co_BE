package co.kr.payment.model.dto.request;

import co.kr.payment.model.vo.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ChargeReq(

        @NotNull(message = "유저 인덱스는 필수입니다.")
        Long userIdx,

        @NotNull(message = "충전 금액은 필수입니다.")
        @Positive(message = "충전 금액은 0보다 커야 합니다.")
        BigDecimal amount,

        @NotNull(message = "결제 수단은 필수입니다.")
        PaymentType paymentType
) {}
