package co.kr.user.controller;

import co.kr.user.model.dto.auth.TokenDto;
import co.kr.user.service.AuthService;
import co.kr.user.util.BaseResponse;
import co.kr.user.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증(Authentication) 관련 요청을 처리하는 컨트롤러입니다.
 * 주로 토큰 재발급(Refresh)과 같은 보안 관련 진입점을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * 만료된 Access Token을 Refresh Token을 사용하여 재발급합니다.
     * 쿠키에 저장된 Refresh Token을 읽어와 유효성을 검증하고, 새로운 토큰 세트를 발급하여 쿠키에 다시 저장합니다.
     * @param refreshToken 요청 쿠키에서 추출한 리프레시 토큰 (없을 경우 null)
     * @param response 응답 객체 (새로운 토큰을 쿠키에 설정하기 위함)
     * @return 성공 메시지를 담은 응답 객체
     */
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<String>> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                                        HttpServletResponse response) {
        // 서비스 계층에 토큰 재발급 요청 위임
        TokenDto tokenDto = authService.refreshToken(refreshToken);

        // 재발급된 Access Token을 쿠키에 추가 (HttpOnly, Path 설정 등은 CookieUtil 내부에서 처리)
        CookieUtil.addCookie(response,
                CookieUtil.ACCESS_TOKEN_NAME,
                tokenDto.getAccessToken(),
                CookieUtil.ACCESS_TOKEN_EXPIRY
        );

        // 만약 Refresh Token도 갱신되었다면(Rotation), 새 Refresh Token을 쿠키에 추가
        if (tokenDto.getRefreshToken() != null) {
            CookieUtil.addCookie(response,
                    CookieUtil.REFRESH_TOKEN_NAME,
                    tokenDto.getRefreshToken(),
                    CookieUtil.REFRESH_TOKEN_EXPIRY
            );
        }

        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", "토큰이 재발급되었습니다."));
    }
}