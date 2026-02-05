package co.kr.order.model.dto;

/*
 * @param addressInfo : 주소 정보 (recipient, address, addressDetail, phoneNum)
 * @param cardInfo : 카드 정보 (cardBrand, cardName, cardToken, exp_month, exp_year)
 */
public record ClientUserDataReq(

        Long userIdx,

        AddressInfo addressInfo,

        CardInfo cardInfo
) {}
