package co.kr.user.service;

import co.kr.user.model.dto.card.CardListDTO;
import co.kr.user.model.dto.card.CardRequestReq;

import java.util.List;

/**
 * 사용자 카드(결제 수단) 관리를 위한 서비스 인터페이스입니다.
 * 카드 목록 조회, 등록, 수정, 삭제 기능을 정의합니다.
 */
public interface CardService {

    /**
     * 특정 사용자가 등록한 카드 목록을 조회합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @return 등록된 카드 정보 목록 DTO 리스트
     */
    List<CardListDTO> cardList(Long userIdx);

    /**
     * 새로운 카드를 등록합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @param cardRequestReq 등록할 카드 정보(브랜드, 이름, 번호 등)가 담긴 요청 객체
     * @return 처리 결과 메시지 ("카드가 성공적으로 추가되었습니다.")
     */
    String addCard(Long userIdx, CardRequestReq cardRequestReq);

    /**
     * 등록된 카드 정보를 수정합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @param cardRequestReq 수정할 카드 정보와 식별 코드(CardCode)가 담긴 요청 객체
     * @return 처리 결과 메시지 ("카드 정보가 수정되었습니다.")
     */
    String updateCard(Long userIdx, CardRequestReq cardRequestReq);

    /**
     * 등록된 카드를 삭제(논리적 삭제)합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @param cardCode 삭제할 카드의 고유 코드
     * @return 처리 결과 메시지 ("카드가 삭제되었습니다.")
     */
    String deleteCard(Long userIdx, String cardCode);
}