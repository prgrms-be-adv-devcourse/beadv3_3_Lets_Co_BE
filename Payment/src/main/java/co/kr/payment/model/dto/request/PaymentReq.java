package co.kr.payment.model.dto.request;

import co.kr.payment.model.vo.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentReq(

        @NotBlank(message = "유저 인데스는 필수입니다.")
        Long userIdx,

        @NotBlank(message = "주문 코드는 필수입니다.")
        String orderCode,

        @NotNull(message = "주문 IDX는 필수입니다.")
        Long ordersIdx,

        @NotNull(message = "결제 타입은 필수입니다.")
        PaymentType paymentType,

        @NotNull(message = "결제 금액은 필수입니다.")
        BigDecimal amount,

        // TOSS_PAY일 때만 사용, CARD/DEPOSIT은 null
        String paymentKey
) {}
