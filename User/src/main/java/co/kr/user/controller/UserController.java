package co.kr.user.controller;

import co.kr.user.dto.response.UserProfileResponse;
import co.kr.user.dto.response.UserResponse;
import co.kr.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [회원 관련 API 컨트롤러]
 * 클라이언트(프론트엔드)로부터 들어오는 회원 관련 HTTP 요청을 처리합니다.
 * Base URL: /users (이 클래스의 모든 API는 /users로 시작합니다)
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    // 비즈니스 로직을 처리하는 서비스 계층 의존성 주입
    private final UserService userService;

    /**
     * 마이페이지 기본 정보 조회 API
     * 요청 URL: GET /users/me
     * 역할: 로그인한 사용자의 핵심 정보(ID, 잔액, 등급 등)를 반환합니다.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyPage() {
        /*
         * [Security 인증 연동 예정]
         * 실제 운영 환경에서는 SecurityContextHolder를 통해 현재 로그인한 사용자의 PK(userId)를 가져와야 함
         * 현재는 개발 및 테스트를 위해 임의의 ID(1L)를 고정으로 사용
         */
        Long currentUserId = 1L;

        // 서비스 계층에 조회를 요청하고 결과를 200 OK 상태 코드와 함께 반환
        return ResponseEntity.ok(userService.getMyPageInfo(currentUserId));
    }

    /**
     * 상세 개인 정보 조회 API
     * 요청 URL: GET /users/me/details
     * 역할: 마이페이지의 '상세 정보' 탭 등에서 사용될 민감하거나 구체적인 정보(이름, 전화번호, 생년월일 등)를 반환합니다.
     */
    @GetMapping("/me/details")
    public ResponseEntity<UserProfileResponse> getMyPageDetails() {
        // [Security 인증 연동 예정] 위와 동일하게 임시 ID 사용
        Long currentUserId = 1L;

        // 서비스 계층에 상세 정보 조회를 요청
        return ResponseEntity.ok(userService.getMyPageDetails(currentUserId));
    }
}


