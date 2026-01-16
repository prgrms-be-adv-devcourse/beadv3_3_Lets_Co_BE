package co.kr.order.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/*
 * @param addressInfo : 주소 정보 (recipient, address, addressDetail, phoneNum)
 * @param cardInfo : 카드 정보 (cardBrand, cardName, cardToken, exp_month, exp_year)
 */
public record UserData(
        Long userIdx,

        @Valid
        @NotNull(message = "주소 정보는 필수입니다.")
        AddressInfo addressInfo,

        @Valid
        @NotNull(message = "카드 정보는 필수입니다.")
        CardInfo cardInfo
) {}
