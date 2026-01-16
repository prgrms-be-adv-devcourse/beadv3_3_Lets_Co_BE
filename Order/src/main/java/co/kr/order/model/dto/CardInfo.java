package co.kr.order.model.dto;

/*
 * @param cardBrand: 카드 브랜드
 * @param cardName: 카드 이름
 * @param cardToken: 카드 토큰
 * @param expMonth: 만료 월
 * @param expYear: 만료 년
 */
public record CardInfo (
        Long cardIdx,
        String cardBrand,
        String cardName,
        String cardToken,
        Integer expMonth,
        Integer expYear
) {}