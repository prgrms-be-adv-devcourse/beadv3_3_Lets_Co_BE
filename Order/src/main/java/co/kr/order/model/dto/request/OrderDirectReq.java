package co.kr.order.model.dto.request;

import co.kr.order.model.dto.AddressInfo;
import co.kr.order.model.dto.CardInfo;
import co.kr.order.model.dto.ProductInfo;
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
        ProductInfo productInfo,

        @Valid
        @NotNull(message = "주소 정보는 필수입니다.")
        AddressInfo addressInfo,

        CardInfo cardInfo,

        @Valid
        @NotNull(message = "결제 방법을 선택하세요")
        PaymentType paymentType,

        String tossKey
) {}
