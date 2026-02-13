package co.kr.user.controller;

import co.kr.user.model.dto.auth.TokenDto;
import co.kr.user.service.AuthService;
import co.kr.user.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                          HttpServletResponse response) {
        TokenDto tokenDto = authService.refreshToken(refreshToken);

        CookieUtil.addCookie(response,
                CookieUtil.ACCESS_TOKEN_NAME,
                tokenDto.getAccessToken(),
                CookieUtil.ACCESS_TOKEN_EXPIRY
        );

        if (tokenDto.getRefreshToken() != null) {
            CookieUtil.addCookie(response,
                    CookieUtil.REFRESH_TOKEN_NAME,
                    tokenDto.getRefreshToken(),
                    CookieUtil.REFRESH_TOKEN_EXPIRY
            );
        }

        return ResponseEntity.ok("토큰이 재발급되었습니다.");
    }
}