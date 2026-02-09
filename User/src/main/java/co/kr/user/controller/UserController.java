package co.kr.user.controller;

import co.kr.user.model.dto.my.*;
import co.kr.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 회원(User) 관련 API 요청을 처리하는 컨트롤러 클래스입니다.
 * 마이페이지 정보 조회, 상세 프로필 확인, 회원 탈퇴(요청/확정), 개인정보 수정 등의 기능을 제공합니다.
 * 모든 요청은 인증된 사용자의 식별자(X-USERS-IDX)를 기반으로 처리됩니다.
 */
@Validated // 요청 파라미터나 바디의 데이터 유효성 검증(Validation)을 활성화하는 어노테이션입니다.
@RestController // 이 클래스가 RESTful API 컨트롤러임을 나타내며, 반환값은 자동으로 JSON 형식으로 변환됩니다.
@RequiredArgsConstructor // final이 붙은 필드(userService)에 대해 생성자를 자동으로 생성해주어 의존성을 주입받게 합니다.
@RequestMapping("/users") // 이 클래스 내의 모든 메서드는 "/users"로 시작하는 URL 경로에 매핑됩니다.
public class UserController {

    // 회원 정보 관리 비즈니스 로직을 처리하는 서비스 객체입니다.
    private final UserService userService;

    /**
     * 내 정보 조회 API (기본 정보)
     * 사용자의 기본적인 정보(아이디, 이름 등 민감하지 않은 정보 위주)를 조회합니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자 (Gateway 등에서 인증 후 주입됨)
     * @return ResponseEntity<UserDTO> 사용자 기본 정보(UserDTO)를 포함한 응답 객체 (HTTP 200 OK)
     */
    @PostMapping("/my")
    public ResponseEntity<UserDTO> my(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        // UserService를 통해 해당 사용자의 기본 정보를 조회하고 반환합니다.
        return ResponseEntity.ok(userService.my(userIdx));
    }

    /**
     * 내 정보 상세 조회 API
     * 사용자의 상세 프로필 정보(연락처, 주소 등 암호화된 민감 정보 포함 가능)를 조회합니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @return ResponseEntity<UserProfileDTO> 사용자 상세 프로필 정보(UserProfileDTO)를 포함한 응답 객체 (HTTP 200 OK)
     */
    @PostMapping("/my/details")
    public ResponseEntity<UserProfileDTO> myDetails(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        // UserService를 통해 해당 사용자의 상세 프로필 정보를 조회하고 반환합니다.
        return ResponseEntity.ok(userService.myDetails(userIdx));
    }

    /**
     * 회원 탈퇴 요청 API (1단계)
     * 회원 탈퇴 절차를 시작하는 요청입니다.
     * 일반적으로 탈퇴를 위한 인증번호를 이메일로 발송하거나, 탈퇴 전 확인 정보를 반환합니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @return ResponseEntity<UserDeleteDTO> 탈퇴 요청에 대한 처리 결과(인증 만료 시간 등)를 담은 객체 (HTTP 200 OK)
     */
    @PostMapping("/my/delete")
    public ResponseEntity<UserDeleteDTO> myDelete(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        // UserService를 통해 회원 탈퇴 요청(인증번호 발송 등) 로직을 수행하고 결과를 반환합니다.
        return ResponseEntity.ok(userService.myDelete(userIdx));
    }

    /**
     * 회원 탈퇴 확정 API (2단계)
     * 1단계에서 발송된 인증 코드를 검증하여, 최종적으로 회원을 탈퇴(삭제 또는 비활성화) 처리합니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @param userDeleteSecondStepReq HTTP Body에 포함된 탈퇴 확인용 데이터 (인증 코드 등)
     * @Valid 어노테이션을 통해 입력 데이터의 유효성을 검증합니다.
     * @return ResponseEntity<String> 탈퇴 처리 완료 메시지를 포함한 응답 객체 (HTTP 200 OK)
     */
    @DeleteMapping("/my/delete")
    public ResponseEntity<String> deleteUser(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                             @RequestBody @Valid UserDeleteSecondStepReq userDeleteSecondStepReq,
                                             HttpServletResponse response) {
        // UserService를 통해 인증 코드(AuthCode)를 검증하고, 일치할 경우 최종 탈퇴 처리를 수행합니다.
        return ResponseEntity.ok(userService.myDelete(userIdx, userDeleteSecondStepReq.getAuthCode(), response));
    }

    /**
     * 내 정보 수정 API
     * 사용자의 개인정보(이름, 전화번호 등)를 수정합니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @param userAmendReq HTTP Body에 포함된 수정할 사용자 정보 데이터
     * @return ResponseEntity<UserAmendReq> 수정 완료된 사용자 정보를 포함한 응답 객체 (HTTP 200 OK)
     */
    @PutMapping("/my/details")
    public ResponseEntity<UserAmendReq> myamend(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                                @RequestBody UserAmendReq userAmendReq) {
        // UserService를 통해 사용자 정보를 업데이트하고, 수정된 결과 데이터를 반환합니다.
        return ResponseEntity.ok(userService.myAmend(userIdx, userAmendReq));
    }
}