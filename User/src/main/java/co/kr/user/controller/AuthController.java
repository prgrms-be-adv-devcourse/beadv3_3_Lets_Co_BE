package co.kr.user.controller;

import co.kr.user.model.DTO.auth.TokenDto;
import co.kr.user.service.AuthService;
import co.kr.user.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증(Auth) 관련 부가 기능을 처리하는 컨트롤러 클래스입니다.
 * 만료된 Access Token을 Refresh Token을 이용하여 재발급(갱신)하는 기능을 제공합니다.
 */
@RestController // RESTful API 컨트롤러임을 명시하며, 응답 데이터를 JSON으로 반환합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성을 주입받습니다.
@RequestMapping("/auth") // 이 클래스의 API 기본 경로를 "/auth"로 설정합니다.
public class AuthController {

    // 인증 관련 비즈니스 로직(권한 확인, 토큰 재발급 등)을 처리하는 서비스 객체입니다.
    private final AuthService authService;

    /**
     * 토큰 재발급(Refresh) API
     * Access Token이 만료되었을 때, 쿠키에 저장된 Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.
     * Refresh Token Rotation(RTR) 정책에 따라 Refresh Token도 함께 갱신될 수 있습니다.
     *
     * @param refreshToken HTTP 쿠키에서 추출한 Refresh Token 값 (@CookieValue 사용)
     * @param response HTTP 응답 객체 (새로운 토큰을 쿠키에 설정하기 위해 사용)
     * @return ResponseEntity<String> 토큰 재발급 성공 메시지를 포함한 응답 객체 (HTTP 200 OK)
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                          HttpServletResponse response) {
        // 1. AuthService를 통해 토큰 재발급 로직을 수행하고, 새로운 토큰 정보(TokenDto)를 받아옵니다.
        TokenDto tokenDto = authService.refreshToken(refreshToken);

        // 2. 새로 발급된 Access Token을 HttpOnly 쿠키에 설정합니다.
        CookieUtil.addCookie(response,
                CookieUtil.ACCESS_TOKEN_NAME,    // 쿠키 이름 (예: access_token)
                tokenDto.getAccessToken(),       // 토큰 값
                CookieUtil.ACCESS_TOKEN_EXPIRY   // 만료 시간
        );

        // 3. Refresh Token이 갱신(Rotation)된 경우, 새로운 Refresh Token을 쿠키에 설정합니다.
        if (tokenDto.getRefreshToken() != null) {
            CookieUtil.addCookie(response,
                    CookieUtil.REFRESH_TOKEN_NAME,   // 쿠키 이름 (예: refresh_token)
                    tokenDto.getRefreshToken(),      // 토큰 값
                    CookieUtil.REFRESH_TOKEN_EXPIRY  // 만료 시간
            );
        }

        // 4. 재발급 성공 메시지를 반환합니다.
        return ResponseEntity.ok("토큰이 재발급되었습니다.");
    }
}