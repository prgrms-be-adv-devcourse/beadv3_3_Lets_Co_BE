package co.kr.order.model.dto.request;

import co.kr.order.model.dto.AddressInfo;
import co.kr.order.model.dto.CardInfo;

public record UserDataRequest (
        AddressInfo addressInfo,
        CardInfo cardInfo
) {}
