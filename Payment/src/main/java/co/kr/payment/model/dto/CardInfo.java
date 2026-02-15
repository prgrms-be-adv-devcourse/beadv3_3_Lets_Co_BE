package co.kr.payment.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * 카드 정보
 * @param cardBrand: 카드 브랜드
 * @param cardName: 카드명
 * @param cardToken: 카드 토큰
 * @param expMonth: 유효기간(월)
 * @param expYear: 유효기간(년)
 */
public record CardInfo (
        @NotBlank(message = "카드 브랜드는 필수입니다.")
        String cardBrand,

        @NotBlank(message = "카드 명의자는 필수입니다.")
        String cardName,

        @NotBlank(message = "카드 토큰은 필수입니다.")
        String cardToken,

        @NotNull(message = "유효기간(월)은 필수입니다.")
        Integer expMonth,

        @NotNull(message = "유효기간(년)은 필수입니다.")
        Integer expYear
) {}