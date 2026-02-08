package co.kr.order.model.dto.request;

import co.kr.order.model.dto.AddressInfo;
import co.kr.order.model.dto.CardInfo;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.vo.OrderType;
import co.kr.order.model.vo.PaymentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OrderReq (

        @Valid
        @NotNull(message = "주문타입은 필수입니다.")
        OrderType orderType,

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
