package co.kr.user.model.dto.card;

import lombok.Data;

/**
 * 사용자가 결제 수단(카드)을 등록하거나 수정 요청을 보낼 때 사용하는 DTO입니다.
 * 카드사 정보, 별칭, PG사 빌링 토큰 및 유효기간 등의 정보를 포함합니다.
 */
@Data
public class CardRequestReq {
    /** 수정 시 대상 카드를 식별하기 위한 고유 코드 (신규 등록 시에는 비어있을 수 있음) */
    private String cardCode;
    /** 해당 카드를 기본 결제 수단으로 설정할지 여부 */
    private boolean defaultCard;
    /** 카드사 브랜드 이름 (예: 현대, 삼성, VISA 등) */
    private String cardBrand;
    /** 사용자가 지정한 카드의 별칭 (예: 생활비 카드) */
    private String cardName;
    /** 결제 대행사(PG)를 통해 발급받은 카드 결제용 빌링 토큰 */
    private String cardToken;
    /** 카드의 유효기간 - 월 */
    private int expMonth;
    /** 카드의 유효기간 - 년 */
    private int expYear;
}