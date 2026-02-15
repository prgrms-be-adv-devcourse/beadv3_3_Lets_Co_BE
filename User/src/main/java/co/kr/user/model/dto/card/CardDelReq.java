package co.kr.user.model.dto.card;

import lombok.Data;

/**
 * 사용자가 등록한 결제 카드를 삭제하고자 할 때 요청 정보를 담는 DTO입니다.
 */
@Data
public class CardDelReq {
    /**
     * 삭제할 카드를 식별하기 위한 고유 코드입니다.
     * 보안을 위해 실제 DB의 PK 대신 이 코드를 사용하여 요청을 처리합니다.
     */
    private String cardCode;
}