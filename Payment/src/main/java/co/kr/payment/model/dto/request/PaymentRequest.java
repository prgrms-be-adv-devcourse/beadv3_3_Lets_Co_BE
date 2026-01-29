package co.kr.payment.model.dto.request;

import co.kr.payment.model.vo.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(

        @NotBlank(message = "주문 코드는 필수입니다.")
        String orderCode,

        @NotNull(message = "결제 타입은 필수입니다.")
        PaymentType paymentType,

        @NotNull(message = "결제 금액은 필수입니다.")
        BigDecimal amount
) {}
