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

/**
 * 일반 사용자(회원) 마이페이지 관련 기능을 처리하는 REST 컨트롤러입니다.
 * 내 정보 조회, 수정, 잔액 확인, 회원 탈퇴 등의 기능을 제공합니다.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users") // "/users" 경로 하위 API
public class UserController {
    private final UserService userService;

    /**
     * 사용자의 현재 잔액(포인트/머니)을 조회합니다.
     * @param userIdx 요청 헤더(X-USERS-IDX)에서 추출한 사용자 식별자
     * @return 현재 잔액을 담은 응답 객체
     */
    @PostMapping("/balance")
    public ResponseEntity<BaseResponse<BigDecimal>> balance(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", userService.balance(userIdx)));
    }

    /**
     * 마이페이지 메인에 표시할 사용자의 요약 정보를 조회합니다.
     * @param userIdx 요청 헤더에서 추출한 사용자 식별자
     * @return 사용자 요약 정보 DTO
     */
    @PostMapping("/my")
    public ResponseEntity<BaseResponse<UserDTO>> my(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", userService.my(userIdx)));
    }

    /**
     * 사용자의 상세 프로필 정보를 조회합니다.
     * @param userIdx 요청 헤더에서 추출한 사용자 식별자
     * @return 사용자 상세 프로필 DTO
     */
    @PostMapping("/my/details")
    public ResponseEntity<BaseResponse<UserProfileDTO>> myDetails(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", userService.myDetails(userIdx)));
    }

    /**
     * [회원 탈퇴 1단계]
     * 회원 탈퇴를 위한 인증 메일을 요청합니다.
     * @param userIdx 요청 헤더에서 추출한 사용자 식별자
     * @return 탈퇴 요청 결과 DTO (인증 메일 정보)
     */
    @PostMapping("/my/delete")
    public ResponseEntity<BaseResponse<UserDeleteDTO>> myDelete(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", userService.myDelete(userIdx)));
    }

    /**
     * [회원 탈퇴 2단계]
     * 인증 코드를 검증하고 회원 탈퇴를 최종 완료합니다. (로그아웃 처리 포함)
     * @param userIdx 요청 헤더에서 추출한 사용자 식별자
     * @param userDeleteSecondStepReq 인증 코드 정보
     * @param response 쿠키 삭제를 위한 HttpServletResponse
     * @return 처리 성공 메시지
     */
    @DeleteMapping("/my/delete")
    public ResponseEntity<BaseResponse<String>> deleteUser(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                                           @RequestBody @Valid UserDeleteSecondStepReq userDeleteSecondStepReq,
                                                           HttpServletResponse response) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", userService.myDelete(userIdx, userDeleteSecondStepReq.getAuthCode(), response)));
    }

    /**
     * 사용자의 회원 정보를 수정합니다.
     * @param userIdx 요청 헤더에서 추출한 사용자 식별자
     * @param userAmendReq 수정할 회원 정보 (이메일, 이름 등)
     * @return 수정된 회원 정보 DTO
     */
    @PutMapping("/my")
    public ResponseEntity<BaseResponse<UserAmendDTO>> userAmendReqResponseEntity(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                                                                 @Valid @RequestBody UserAmendReq userAmendReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", userService.myAmend(userIdx, userAmendReq)));
    }
}