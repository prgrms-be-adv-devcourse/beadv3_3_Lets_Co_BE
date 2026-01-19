package co.kr.user.controller;

import co.kr.user.model.DTO.auth.TokenDto;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.service.AuthService;
import co.kr.user.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 인증(Authentication) 및 인가(Authorization) 관련 컨트롤러
 * * <p>사용자의 역할(Role) 조회와 만료된 액세스 토큰을 갱신(Refresh)하는 기능을 제공합니다.</p>
 */
@Validated // 파라미터 유효성 검증 활성화
@RestController // JSON 응답을 반환하는 컨트롤러
@RequiredArgsConstructor // 생성자 주입(DI) 자동화
@RequestMapping("/auth") // 기본 경로: /auth
public class AuthController {
    // 인증 관련 비즈니스 로직을 수행하는 서비스
    private final AuthService authService;

    /**
     * 사용자 권한(Role) 조회 API
     * * <p>HTTP Method: GET</p>
     * <p>Path: /auth/role</p>
     * * @param userIdx 조회할 사용자의 고유 식별자 (Query Parameter, 예: ?userIdx=1)
     * @return 사용자의 권한 정보(UsersRole Enum 등)를 반환 (200 OK)
     */
    @GetMapping("/role")
    public ResponseEntity<UsersRole> getRole(@RequestParam @Valid Long userIdx) {
        // 서비스 계층을 호출하여 해당 userIdx를 가진 유저의 역할(ADMIN, USER 등)을 조회합니다.
        return ResponseEntity.ok(authService.getRole(userIdx));
    }

    /**
     * 토큰 갱신(Refresh Token) API
     * * <p>쿠키에 저장된 Refresh Token을 이용하여 새로운 Access Token(및 Refresh Token)을 발급받습니다.</p>
     * <p>HTTP Method: POST</p>
     * <p>Path: /auth/refresh</p>
     * * @param refreshToken 쿠키에서 추출한 리프레시 토큰 값 (필수 아님, 없을 경우 null)
     * @param response 쿠키 설정을 위한 HttpServletResponse 객체
     * @return 갱신 성공 시 성공 메시지(200), 실패 시 에러 메시지(401)
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@CookieValue(name = CookieUtil.REFRESH_TOKEN_NAME, required = false) String refreshToken,
                                          HttpServletResponse response) {
        // 1. 쿠키에 리프레시 토큰이 존재하는지 1차 확인
        if (refreshToken == null) {
            return ResponseEntity.status(401).body("Refresh Token이 존재하지 않습니다.");
        }

        try {
            // 2. 서비스 계층을 통해 토큰 검증 및 재발급 진행
            TokenDto tokenDto = authService.refreshToken(refreshToken);

            // 3. 재발급된 Access Token을 쿠키에 저장
            CookieUtil.addCookie(response,
                    CookieUtil.ACCESS_TOKEN_NAME,
                    tokenDto.getAccessToken(),
                    CookieUtil.ACCESS_TOKEN_EXPIRY);

            // 4. Refresh Token이 함께 재발급(Rotation)된 경우, 쿠키 업데이트
            // (보안 정책에 따라 Refresh Token도 갱신될 수 있음)
            if (tokenDto.getRefreshToken() != null) {
                CookieUtil.addCookie(response,
                        CookieUtil.REFRESH_TOKEN_NAME,
                        tokenDto.getRefreshToken(),
                        CookieUtil.REFRESH_TOKEN_EXPIRY);
            }

            return ResponseEntity.ok("토큰이 갱신되었습니다.");

        } catch (IllegalArgumentException e) {
            // 5. 토큰 갱신 실패 시(예: 만료됨, 위변조됨 등) 기존 쿠키 삭제 처리 (보안 조치)
            CookieUtil.deleteCookie(response, CookieUtil.ACCESS_TOKEN_NAME);
            CookieUtil.deleteCookie(response, CookieUtil.REFRESH_TOKEN_NAME);

            // 401 Unauthorized 상태 코드 반환
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}