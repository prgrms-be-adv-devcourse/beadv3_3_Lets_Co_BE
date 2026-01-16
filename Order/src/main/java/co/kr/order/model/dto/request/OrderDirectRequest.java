package co.kr.order.model.dto.request;

import co.kr.order.model.dto.UserData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/*
 * @param orderRequest : productIdx, optionIdx, quantity
 * @param userData : 주소정보, 카그정보
 */
public record OrderDirectRequest(

        @Valid
        @NotNull(message = "주문 정보는 필수입니다.")
        OrderRequest orderRequest,

        @Valid
        @NotNull(message = "사용자 정보(주소/카드)는 필수입니다.")
        UserData userData
) {}
