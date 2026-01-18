package co.kr.user.controller;

import co.kr.user.model.DTO.my.*;
import co.kr.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/my")
    public ResponseEntity<UserDTO> my(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(userService.my(userIdx));
    }

    @PostMapping("/my/details")
    public ResponseEntity<UserProfileDTO> myDetails(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(userService.myDetails(userIdx));
    }

    @PostMapping("/my/delete")
    public ResponseEntity<UserDeleteDTO> myDelete(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(userService.myDelete(userIdx));
    }

    @DeleteMapping("/my/delete")
    public ResponseEntity<String> deleteUser(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                             @RequestBody @Valid UserDeleteSecondStepReq userDeleteSecondStepReq) {
        return ResponseEntity.ok(userService.myDelete(userIdx, userDeleteSecondStepReq.getAuthCode()));
    }

    @PutMapping("/my/details")
    public ResponseEntity<UserAmendReq> myamend(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                                @RequestBody UserAmendReq userAmendReq) {
        return ResponseEntity.ok(userService.myAmend(userIdx, userAmendReq));
    }
}