package co.kr.user.model.dto.card;

import lombok.Data;

/**
 * 시스템 내부 또는 클라이언트와의 통신에서 사용되는 일반적인 카드 정보 데이터 객체입니다.
 */
@Data
public class CardDTO {
    /** 카드 식별을 위한 고유 코드 (UUID 등) */
    private String cardCode;
    /** 기본 결제 수단 여부 (1: 기본, 0: 일반) */
    private int defaultCard;
    /** 카드사 브랜드 (예: VISA, MASTER, BC 등) */
    private String cardBrand;
    /** 사용자가 설정하거나 카드사에서 제공하는 카드 명칭 */
    private String cardName;
    /** 결제 대행사(PG)로부터 발급받은 카드 빌링 토큰 */
    private String cardToken;
    /** 카드 유효기간 - 월 (1~12) */
    private int expMonth;
    /** 카드 유효기간 - 년 (YYYY) */
    private int expYear;
}