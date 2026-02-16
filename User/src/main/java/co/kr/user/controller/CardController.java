package co.kr.user.controller;

import co.kr.user.model.dto.card.*;
import co.kr.user.service.CardService;
import co.kr.user.util.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 결제 카드 관리를 위한 REST 컨트롤러입니다.
 * 카드 목록 조회, 등록, 수정, 삭제 API를 제공합니다.
 * Gateway 등 앞단에서 인증 후 헤더로 전달된 "X-USERS-IDX"를 사용하여 사용자 식별자를 획득합니다.
 */
@Validated // 요청 데이터 유효성 검증 활성화
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/card")
public class CardController {
    private final CardService cardService;

    /**
     * 사용자의 등록된 카드 목록을 조회합니다.
     * @param userIdx 요청 헤더에서 추출한 사용자 식별자
     * @return 카드 목록 DTO 리스트를 담은 응답 객체
     */
    @PostMapping("/list")
    public ResponseEntity<BaseResponse<List<CardListDTO>>> cardList(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", cardService.cardList(userIdx)));
    }

    /**
     * 새로운 카드를 등록합니다.
     * @param userIdx 요청 헤더에서 추출한 사용자 식별자
     * @param cardRequestReq 등록할 카드 정보 (번호, 유효기간, 브랜드 등)
     * @return 처리 성공 메시지를 담은 응답 객체
     */
    @PostMapping("/add")
    public ResponseEntity<BaseResponse<String>> addCard(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                        @RequestBody CardRequestReq cardRequestReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", cardService.addCard(userIdx, cardRequestReq)));
    }

    /**
     * 등록된 카드 정보를 수정합니다.
     * @param userIdx 요청 헤더에서 추출한 사용자 식별자
     * @param cardRequestReq 수정할 카드 정보 (카드 코드 포함)
     * @return 처리 성공 메시지를 담은 응답 객체
     */
    @PutMapping("/update")
    public ResponseEntity<BaseResponse<String>> updateCard(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                           @RequestBody CardRequestReq cardRequestReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", cardService.updateCard(userIdx, cardRequestReq)));
    }

    /**
     * 등록된 카드를 삭제합니다.
     * @param userIdx 요청 헤더에서 추출한 사용자 식별자
     * @param cardDelReq 삭제할 카드 코드 정보 (@Valid로 유효성 검사 수행)
     * @return 처리 성공 메시지를 담은 응답 객체
     */
    @DeleteMapping("/delete")
    public ResponseEntity<BaseResponse<String>> deleteCard(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                           @RequestBody @Valid CardDelReq cardDelReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", cardService.deleteCard(userIdx, cardDelReq.getCardCode())));
    }
}