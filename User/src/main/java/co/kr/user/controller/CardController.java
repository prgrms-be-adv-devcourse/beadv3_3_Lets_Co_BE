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

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/card")
public class CardController {
    private final CardService cardService;

    @PostMapping("/list")
    public ResponseEntity<BaseResponse<List<CardListDTO>>> cardList(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", cardService.cardList(userIdx)));
    }

    @PostMapping("/add")
    public ResponseEntity<BaseResponse<String>> addCard(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                        @RequestBody CardRequestReq cardRequestReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", cardService.addCard(userIdx, cardRequestReq)));
    }

    @PutMapping("/update")
    public ResponseEntity<BaseResponse<String>> updateCard(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                           @RequestBody CardRequestReq cardRequestReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", cardService.updateCard(userIdx, cardRequestReq)));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<BaseResponse<String>> deleteCard(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                           @RequestBody @Valid CardDelReq cardDelReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", cardService.deleteCard(userIdx, cardDelReq.getCardCode())));
    }
}