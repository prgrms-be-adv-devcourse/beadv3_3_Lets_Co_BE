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

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @GetMapping("/role")
    public ResponseEntity<UsersRole> getRole(@RequestParam @Valid Long userIdx) {
        return ResponseEntity.ok(authService.getRole(userIdx));
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@CookieValue(name = CookieUtil.REFRESH_TOKEN_NAME, required = false) String refreshToken,
                                          HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.status(401).body("Refresh Token이 존재하지 않습니다.");
        }

        try {
            TokenDto tokenDto = authService.refreshToken(refreshToken);

            CookieUtil.addCookie(response,
                    CookieUtil.ACCESS_TOKEN_NAME,
                    tokenDto.getAccessToken(),
                    CookieUtil.ACCESS_TOKEN_EXPIRY);

            if (tokenDto.getRefreshToken() != null) {
                CookieUtil.addCookie(response,
                        CookieUtil.REFRESH_TOKEN_NAME,
                        tokenDto.getRefreshToken(),
                        CookieUtil.REFRESH_TOKEN_EXPIRY);
            }

            return ResponseEntity.ok("토큰이 갱신되었습니다.");

        } catch (IllegalArgumentException e) {
            CookieUtil.deleteCookie(response, CookieUtil.ACCESS_TOKEN_NAME);
            CookieUtil.deleteCookie(response, CookieUtil.REFRESH_TOKEN_NAME);
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}