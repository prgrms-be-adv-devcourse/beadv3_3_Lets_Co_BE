package co.kr.payment.model.dto.request;

import co.kr.payment.model.dto.UserInfo;
import co.kr.payment.model.vo.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentReq(

        @NotBlank(message = "주문 코드는 필수입니다.")
        String orderCode,

        @NotNull(message = "유저 정보는 필수입니다.")
        UserInfo userInfo,

        @NotNull(message = "결제 타입은 필수입니다.")
        PaymentType paymentType,

        @NotNull(message = "결제 금액은 필수입니다.")
        @Positive(message = "결제 금액은 0보다 커야 합니다.")
        BigDecimal amount,

        // TOSS_PAY일 때만 사용, CARD/DEPOSIT은 null
        String tossKey
) {}
