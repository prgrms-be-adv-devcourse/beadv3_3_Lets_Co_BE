package co.kr.user.controller;

import co.kr.user.model.dto.card.*;
import co.kr.user.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카드(결제 수단) 관련 API 요청을 처리하는 컨트롤러 클래스입니다.
 * 사용자의 결제 카드를 등록, 조회, 수정, 삭제하고 기본 결제 수단을 관리하는 기능을 제공합니다.
 * 모든 요청은 인증된 사용자(X-USERS-IDX)를 대상으로 처리됩니다.
 */
@Validated // 요청 데이터(파라미터, 바디)의 유효성 검증(Validation) 기능을 활성화합니다.
@RestController // RESTful API 컨트롤러임을 나타내며, 응답 데이터를 JSON 형식으로 반환합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성을 주입(DI)받습니다.
@RequestMapping("/users/card") // 이 클래스의 API 기본 경로를 "/users/card"로 설정합니다.
public class CardController {

    // 카드 관리 비즈니스 로직을 처리하는 서비스 객체입니다.
    private final CardService cardService;

    /**
     * 기본 카드 조회 API
     * 사용자가 '기본 결제 수단'으로 설정해 둔 카드의 고유 식별자(ID)를 조회합니다.
     * 결제 시 별도의 카드 선택이 없을 경우 이 카드가 사용됩니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @return ResponseEntity<Long> 기본 카드의 고유 ID를 포함한 응답 객체 (HTTP 200 OK)
     */
    @PostMapping("/default")
    public ResponseEntity<Long> defaultCard(@RequestHeader("X-USERS-IDX") Long userIdx) {
        // CardService를 호출하여 사용자의 기본 카드 ID를 반환합니다.
        return ResponseEntity.ok(cardService.defaultCard(userIdx));
    }

    /**
     * 특정 카드 검색 API
     * 카드 고유 코드(Card Code, UUID 등)를 이용하여 해당 카드의 실제 식별자(ID)를 조회합니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @param cardReq HTTP Body에 포함된 카드 검색 정보 (카드 코드 등)
     * @return ResponseEntity<Long> 검색된 카드의 고유 ID를 포함한 응답 객체 (HTTP 200 OK)
     */
    @PostMapping("/search")
    public ResponseEntity<Long> searchCard(@RequestHeader("X-USERS-IDX") Long userIdx, @RequestBody CardReq cardReq) {
        // CardService를 호출하여 카드 코드로 카드 식별자를 조회하고 반환합니다.
        return ResponseEntity.ok(cardService.searchCard(userIdx, cardReq.getCardCode()));
    }

    /**
     * 내 카드 목록 조회 API
     * 사용자가 등록한 모든 카드(결제 수단)의 목록을 조회합니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @return ResponseEntity<List<CardListDTO>> 등록된 카드 정보(CardListDTO) 리스트를 포함한 응답 객체 (HTTP 200 OK)
     */
    @PostMapping("/list")
    public ResponseEntity<List<CardListDTO>> cardList(@RequestHeader("X-USERS-IDX") Long userIdx) {
        // CardService를 호출하여 사용자의 전체 카드 목록을 반환합니다.
        return ResponseEntity.ok(cardService.cardList(userIdx));
    }

    /**
     * 카드 등록 API
     * 사용자의 새로운 결제 카드를 시스템에 등록합니다.
     * 카드 정보는 보안을 위해 서비스 계층에서 암호화되어 저장됩니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @param cardRequestReq HTTP Body에 포함된 등록할 카드 상세 정보 (카드 번호, 유효기간, 별칭 등)
     * @return ResponseEntity<String> 카드 등록 결과 메시지를 포함한 응답 객체 (HTTP 200 OK)
     */
    @PostMapping("/add")
    public ResponseEntity<String> addCard(@RequestHeader("X-USERS-IDX") Long userIdx,
                                          @RequestBody CardRequestReq cardRequestReq) {
        // CardService를 호출하여 카드 등록 로직을 수행하고 결과 메시지를 반환합니다.
        return ResponseEntity.ok(cardService.addCard(userIdx, cardRequestReq));
    }

    /**
     * 카드 정보 수정 API
     * 기존에 등록된 카드의 정보를 수정합니다 (예: 별칭 변경, 유효기간 갱신 등).
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @param cardRequestReq HTTP Body에 포함된 수정할 카드 정보 (카드 코드 필수 포함)
     * @return ResponseEntity<String> 카드 수정 결과 메시지를 포함한 응답 객체 (HTTP 200 OK)
     */
    @PutMapping("/update")
    public ResponseEntity<String> updateCard(@RequestHeader("X-USERS-IDX") Long userIdx,
                                             @RequestBody CardRequestReq cardRequestReq) {
        // CardService를 호출하여 카드 정보 수정 로직을 수행하고 결과 메시지를 반환합니다.
        return ResponseEntity.ok(cardService.updateCard(userIdx, cardRequestReq));
    }

    /**
     * 카드 삭제 API
     * 등록된 카드를 삭제합니다. (실제 데이터 삭제 대신 Soft Delete 처리될 수 있음)
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @param cardDelReq HTTP Body에 포함된 삭제할 카드 정보 (카드 코드 등)
     * @Valid 어노테이션을 통해 삭제 요청 데이터의 유효성을 검증합니다.
     * @return ResponseEntity<String> 카드 삭제 결과 메시지를 포함한 응답 객체 (HTTP 200 OK)
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteCard(@RequestHeader("X-USERS-IDX") Long userIdx,
                                             @RequestBody @Valid CardDelReq cardDelReq) {
        // CardService를 호출하여 카드 삭제 로직을 수행하고 결과 메시지를 반환합니다.
        return ResponseEntity.ok(cardService.deleteCard(userIdx, cardDelReq.getCardCode()));
    }
}