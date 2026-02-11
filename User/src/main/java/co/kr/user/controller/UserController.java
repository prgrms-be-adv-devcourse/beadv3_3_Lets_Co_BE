package co.kr.user.controller;

import co.kr.user.model.dto.my.*;
import co.kr.user.service.UserService;
import co.kr.user.util.BaseResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/balance")
    public ResponseEntity<BaseResponse<BigDecimal>> balance(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", userService.balance(userIdx)));
    }

    @PostMapping("/my")
    public ResponseEntity<BaseResponse<UserDTO>> my(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", userService.my(userIdx)));
    }

    @PostMapping("/my/details")
    public ResponseEntity<BaseResponse<UserProfileDTO>> myDetails(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", userService.myDetails(userIdx)));
    }

    @PostMapping("/my/delete")
    public ResponseEntity<BaseResponse<UserDeleteDTO>> myDelete(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", userService.myDelete(userIdx)));
    }

    @DeleteMapping("/my/delete")
    public ResponseEntity<BaseResponse<String>> deleteUser(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                                           @RequestBody @Valid UserDeleteSecondStepReq userDeleteSecondStepReq,
                                                           HttpServletResponse response) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", userService.myDelete(userIdx, userDeleteSecondStepReq.getAuthCode(), response)));
    }

    @PutMapping("/my")
    public ResponseEntity<BaseResponse<UserAmendReq>> userAmendReqResponseEntity(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                                                                 @RequestBody UserAmendReq userAmendReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", userService.myAmend(userIdx, userAmendReq)));
    }
}