package co.kr.user.controller;

import co.kr.user.model.DTO.card.*;
import co.kr.user.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카드(결제 수단) 관련 API 요청을 처리하는 컨트롤러 클래스입니다.
 * 카드 등록, 조회, 수정, 삭제 및 기본 결제 수단 관리 기능을 제공합니다.
 */
@Validated // 요청 데이터의 유효성 검증을 활성화합니다.
@RestController // RESTful API 컨트롤러임을 나타내며, 응답을 JSON으로 반환합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
@RequestMapping("/users/card") // 이 클래스의 API 기본 경로를 "/users/card"로 설정합니다.
public class CardController {
    // 카드 관련 비즈니스 로직을 처리하는 서비스 객체입니다.
    private final CardService cardService;

    /**
     * 기본 카드 조회 API
     * 사용자의 기본 결제 수단으로 설정된 카드의 식별자(ID)를 조회합니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @return ResponseEntity<Long> 기본 카드의 고유 ID를 포함한 응답 객체
     */
    @PostMapping("/default")
    public ResponseEntity<Long> defaultCard(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(cardService.defaultCard(userIdx));
    }

    /**
     * 특정 카드 검색 API
     * 카드 고유 코드를 이용하여 특정 카드의 ID를 조회합니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @param cardReq HTTP Body에 포함된 카드 검색 정보 (카드 코드 등)
     * @return ResponseEntity<Long> 검색된 카드의 고유 ID를 포함한 응답 객체
     */
    @PostMapping("/search")
    public ResponseEntity<Long> searchCard(@RequestHeader("X-USERS-IDX") Long userIdx, @RequestBody CardReq cardReq) {
        return ResponseEntity.ok(cardService.searchCard(userIdx, cardReq.getCardCode()));
    }

    /**
     * 내 카드 목록 조회 API
     * 사용자가 등록한 모든 카드의 목록을 조회합니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @return ResponseEntity<List<CardListDTO>> 등록된 카드 목록(CardListDTO 리스트)을 포함한 응답 객체
     */
    @PostMapping("/list")
    public ResponseEntity<List<CardListDTO>> cardList(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(cardService.cardList(userIdx));
    }

    /**
     * 카드 등록 API
     * 새로운 결제 카드를 등록합니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @param cardRequestReq HTTP Body에 포함된 등록할 카드 정보 (카드 번호, 만료일 등)
     * @return ResponseEntity<String> 카드 등록 결과 메시지를 포함한 응답 객체
     */
    @PostMapping("/add")
    public ResponseEntity<String> addCard(@RequestHeader("X-USERS-IDX") Long userIdx,
                                          @RequestBody CardRequestReq cardRequestReq) {
        return ResponseEntity.ok(cardService.addCard(userIdx, cardRequestReq));
    }

    /**
     * 카드 정보 수정 API
     * 기존에 등록된 카드의 정보를 수정합니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @param cardRequestReq HTTP Body에 포함된 수정할 카드 정보
     * @return ResponseEntity<String> 카드 수정 결과 메시지를 포함한 응답 객체
     */
    @PutMapping("/update")
    public ResponseEntity<String> updateCard(@RequestHeader("X-USERS-IDX") Long userIdx,
                                             @RequestBody CardRequestReq cardRequestReq) {
        return ResponseEntity.ok(cardService.updateCard(userIdx, cardRequestReq));
    }

    /**
     * 카드 삭제 API
     * 등록된 카드를 삭제합니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @param cardDelReq HTTP Body에 포함된 삭제할 카드 정보 (카드 코드 등)
     * @Valid 삭제 요청 데이터의 유효성을 검증합니다.
     * @return ResponseEntity<String> 카드 삭제 결과 메시지를 포함한 응답 객체
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteCard(@RequestHeader("X-USERS-IDX") Long userIdx,
                                             @RequestBody @Valid CardDelReq cardDelReq) {
        return ResponseEntity.ok(cardService.deleteCard(userIdx, cardDelReq.getCardCode()));
    }
}