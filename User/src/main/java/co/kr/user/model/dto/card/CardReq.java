package co.kr.user.model.dto.card;

import lombok.Data;

/**
 * 카드 코드를 전달하여 특정 카드 정보를 조회하거나 결제 수단으로 선택할 때 사용하는 DTO입니다.
 */
@Data
public class CardReq {
    /** 조회의 대상이 되는 카드의 고유 식별 코드 */
    private String cardCode;
}