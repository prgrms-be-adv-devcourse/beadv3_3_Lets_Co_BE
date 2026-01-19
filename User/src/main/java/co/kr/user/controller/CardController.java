package co.kr.user.controller;

import co.kr.user.model.DTO.card.*;
import co.kr.user.service.CardService;
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

    @PostMapping("/default")
    public ResponseEntity<Long> defaultCard(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(cardService.defaultCard(userIdx));
    }

    @PostMapping("/search")
    public ResponseEntity<Long> searchCard(@RequestHeader("X-USERS-IDX") Long userIdx, @RequestBody CardReq cardReq) {
        return ResponseEntity.ok(cardService.searchCard(userIdx, cardReq.getCardCode()));
    }

    @PostMapping("/list")
    public ResponseEntity<List<CardListDTO>> cardList(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(cardService.cardList(userIdx));
    }

    @PostMapping("/add")
    public ResponseEntity<String> addCard(@RequestHeader("X-USERS-IDX") Long userIdx,
                                          @RequestBody CardRequestReq cardRequestReq) {
        return ResponseEntity.ok(cardService.addCard(userIdx, cardRequestReq));
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateCard(@RequestHeader("X-USERS-IDX") Long userIdx,
                                             @RequestBody CardRequestReq cardRequestReq) {
        return ResponseEntity.ok(cardService.updateCard(userIdx, cardRequestReq));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteCard(@RequestHeader("X-USERS-IDX") Long userIdx,
                                             @RequestBody @Valid CardDelReq cardDelReq) {
        return ResponseEntity.ok(cardService.deleteCard(userIdx, cardDelReq.getCardCode()));
    }
}