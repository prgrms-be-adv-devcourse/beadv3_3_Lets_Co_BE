package co.kr.order.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * @param cardBrand: 카드 브랜드
 * @param cardName: 카드 이름
 * @param cardToken: 카드 토큰
 * @param expMonth: 만료 월
 * @param expYear: 만료 년
 */
public record CardInfo (
        Long cardIdx,

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