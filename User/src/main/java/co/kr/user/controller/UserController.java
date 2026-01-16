package co.kr.user.controller;

import co.kr.user.model.DTO.my.UserDeleteDTO;
import co.kr.user.model.DTO.my.UserDeleteSecondStepReq;
import co.kr.user.model.DTO.my.UserProfileDTO;
import co.kr.user.model.DTO.my.UserDTO;
import co.kr.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * [회원 관련 API 컨트롤러]
 * 클라이언트(프론트엔드)로부터 들어오는 회원 관련 HTTP 요청을 처리합니다.
 * Base URL: /users (이 클래스의 모든 API는 /users로 시작합니다)
 */
@Slf4j // Lombok: 로그를 남기기 위한 log 객체를 자동으로 생성해줍니다.
@Validated // Controller 레벨에서 @RequestParam 등의 유효성 검증(Validation)을 활성화합니다.
@RestController // 이 클래스가 REST API용 컨트롤러임을 명시 (모든 메서드의 리턴값이 JSON 형태가 됨)
@RequiredArgsConstructor // final이 붙은 필드에 대해 생성자를 자동으로 만들어주어 의존성을 주입받습니다.
@RequestMapping("/users") // 이 컨트롤러의 기본 URL 경로를 '/users'로 설정합니다. (예: /users/me)
public class UserController {

    // 비즈니스 로직을 처리하는 서비스 계층 의존성 주입
    private final UserService userService;

    /**
     * 마이페이지 기본 정보 조회 API
     * 요청 URL: GET /users/me
     * 역할: 로그인한 사용자의 핵심 정보(ID, 잔액, 등급 등)를 반환합니다.
     */
    @PostMapping("/my")
    public ResponseEntity<UserDTO> my(@RequestHeader ("X-USERS-IDX") Long user_Idx) {
        log.info("=======================================================");
        log.info("my - My Page Request");
        log.info("user_Idx : {}", user_Idx);
        log.info("=======================================================");

        // 서비스 계층에 조회를 요청하고 결과를 200 OK 상태 코드와 함께 반환
        return ResponseEntity.ok(userService.my(user_Idx));
    }

    /**
     * 상세 개인 정보 조회 API
     * 요청 URL: GET /users/me/details
     * 역할: 마이페이지의 '상세 정보' 탭 등에서 사용될 민감하거나 구체적인 정보(이름, 전화번호, 생년월일 등)를 반환합니다.
     */
    @PostMapping("/my/details")
    public ResponseEntity<UserProfileDTO> myDetails(@RequestHeader ("X-USERS-IDX") Long user_Idx) {
        log.info("=======================================================");
        log.info("meDetails - My Page Detailed Info Request");
        log.info("user_Idx : {}", user_Idx);
        log.info("=======================================================");

        // 서비스 계층에 상세 정보 조회를 요청
        return ResponseEntity.ok(userService.myDetails(user_Idx));
    }

    @PostMapping("/my/delete")
    public ResponseEntity<UserDeleteDTO> meDelete(@RequestHeader ("X-USERS-IDX") Long user_Idx) {
        log.info("=======================================================");
        log.info("meDelete - My Page Delete Info Request");
        log.info("user_Idx : {}", user_Idx);
        log.info("=======================================================");

        // 서비스 계층에 상세 정보 조회를 요청
        return ResponseEntity.ok(userService.myDelete(user_Idx));
    }

    @DeleteMapping("/my/delete")
    public ResponseEntity<String> deleteUser(@RequestHeader ("X-USERS-IDX")
                                             Long user_Idx,
                                             @RequestBody @Valid UserDeleteSecondStepReq userDeleteSecondStepReq) {
        log.info("=======================================================");
        log.info("deleteUser - My Page Delete Info Request");
        log.info("user_Idx : {}", user_Idx);
        log.info("authCode : {}", userDeleteSecondStepReq.getAuthCode());
        log.info("=======================================================");

        return ResponseEntity.ok(userService.myDelete(user_Idx, userDeleteSecondStepReq.getAuthCode()));
    }
}


