package co.kr.order.model.dto.request;

import co.kr.order.model.vo.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(

        @NotBlank(message = "주문 코드는 필수입니다.")
        String orderCode,

        @NotNull(message = "결제 타입은 필수입니다.")
        PaymentType paymentType
) {}
