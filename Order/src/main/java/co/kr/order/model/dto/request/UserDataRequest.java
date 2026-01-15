package co.kr.order.model.dto.request;

import co.kr.order.model.dto.AddressInfo;
import co.kr.order.model.dto.CardInfo;

/*
 * @param addressInfo : 주소 정보 (recipient, address, addressDetail, phoneNum)
 * @param cardInfo : 카드 정보 (cardBrand, cardName, cardToken, exp_month, exp_year)
 */
public record UserDataRequest (
        AddressInfo addressInfo,
        CardInfo cardInfo
) {}
