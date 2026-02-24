package co.kr.user.model.dto.card;

import lombok.Data;

/**
 * 사용자의 마이페이지 또는 결제 화면에서 등록된 카드 목록을 표시하기 위한 DTO입니다.
 */
@Data
public class CardListDTO {
    /** 기본 결제 카드 여부 (1: 기본, 0: 일반) */
    private int defaultCard;
    /** 카드 식별 고유 코드 */
    private String cardCode;
    /** 카드사 브랜드 */
    private String cardBrand;
    /** 카드 명칭 */
    private String cardName;
    /** 보안 처리된 카드 토큰 정보 */
    private String cardToken;
    /** 카드 유효기간 - 월 */
    private int expMonth;
    /** 카드 유효기간 - 년 */
    private int expYear;
}