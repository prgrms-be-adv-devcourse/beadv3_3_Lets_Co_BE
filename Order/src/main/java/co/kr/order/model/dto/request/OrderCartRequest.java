package co.kr.order.model.dto.request;

import co.kr.order.model.dto.UserData;
import co.kr.order.model.vo.PaymentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OrderCartRequest (

    @Valid
    @NotNull(message = "사용자 정보(주소/카드)는 필수입니다.")
    UserData userData,

    @Valid
    @NotNull(message = "결제 방법을 선택하세요")
    PaymentType paymentType
) {}
