package co.kr.user.service;

import co.kr.user.model.dto.card.CardListDTO;
import co.kr.user.model.dto.card.CardRequestReq;

import java.util.List;

/**
 * 사용자 카드(결제 수단) 관리 비즈니스 로직을 정의하는 인터페이스입니다.
 * 카드 목록 조회, 등록, 수정, 삭제, 기본 카드 설정 및 검색 기능을 명세합니다.
 * 구현체: CardServiceImpl
 */
public interface CardService {

    /**
     * 기본 카드 조회 메서드 정의입니다.
     * 사용자가 기본 결제 수단으로 설정한 카드의 식별자(ID)를 조회합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @return 기본 카드 ID (CardIdx)
     */
    Long defaultCard(Long userIdx);

    /**
     * 카드 검색 메서드 정의입니다.
     * 카드 코드(CardCode)를 통해 특정 카드의 식별자(ID)를 조회합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param cardCode 검색할 카드 코드 (UUID)
     * @return 해당 카드 ID (CardIdx)
     */
    Long searchCard(Long userIdx, String cardCode);

    /**
     * 카드 목록 조회 메서드 정의입니다.
     * 사용자가 등록한 모든 카드 정보를 조회합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @return 카드 목록 (CardListDTO 리스트)
     */
    List<CardListDTO> cardList(Long userIdx);

    /**
     * 카드 등록 메서드 정의입니다.
     * 새로운 결제 카드를 등록합니다. (카드 정보 암호화 포함)
     *
     * @param userIdx 사용자 고유 식별자
     * @param cardRequestReq 등록할 카드 정보
     * @return 카드 등록 결과 메시지
     */
    String addCard(Long userIdx, CardRequestReq cardRequestReq);

    /**
     * 카드 수정 메서드 정의입니다.
     * 기존 카드 정보를 수정합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param cardRequestReq 수정할 카드 정보
     * @return 카드 수정 결과 메시지
     */
    String updateCard(Long userIdx, CardRequestReq cardRequestReq);

    /**
     * 카드 삭제 메서드 정의입니다.
     * 등록된 카드를 삭제(Soft Delete)합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param CardCode 삭제할 카드 코드
     * @return 카드 삭제 결과 메시지
     */
    String deleteCard(Long userIdx, String CardCode);
}