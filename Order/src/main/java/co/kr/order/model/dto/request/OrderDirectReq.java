package co.kr.order.model.dto.request;

import co.kr.order.model.vo.PaymentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/*
 * @param orderRequest : productIdx, optionIdx, quantity
 * @param userData : 주소정보, 카드정보
 */
public record OrderDirectReq(

        @Valid
        @NotNull(message = "주문 정보는 필수입니다.")
        OrderReq orderReq,

//        @Valid
//        @NotNull(message = "사용자 정보(주소)는 필수입니다.")
//        UserData userData,

        @Valid
        @NotNull(message = "결제 방법을 선택하세요")
        PaymentType paymentType,

        String tossKey
) {}
