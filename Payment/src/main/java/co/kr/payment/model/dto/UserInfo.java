package co.kr.payment.model.dto;

import jakarta.validation.constraints.NotNull;

public record UserInfo (
        @NotNull(message = "주소 정보는 필수입니다.")
        AddressInfo addressInfo,
        CardInfo cardInfo
) {}
