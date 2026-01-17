package co.kr.user.controller;

import co.kr.user.model.DTO.auth.TokenDto;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.service.AuthService;
import co.kr.user.util.CookieUtil;
import co.kr.user.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j // Lombok: 로그를 남기기 위한 log 객체를 자동으로 생성해줍니다.
@Validated // Controller 레벨에서 @RequestParam 등의 유효성 검증(Validation)을 활성화합니다.
@RestController // 이 클래스가 REST API용 컨트롤러임을 명시 (모든 메서드의 리턴값이 JSON 형태가 됨)
@RequiredArgsConstructor // final이 붙은 필드에 대해 생성자를 자동으로 만들어주어 의존성을 주입받습니다.
@RequestMapping("/auth") // 이 컨트롤러의 기본 URL 경로를 '/auth'로 설정합니다. (예: /auth/signup)
public class AuthController {
    private final AuthService authService;

    private final CookieUtil cookieUtil;

    @GetMapping("/role")
    public ResponseEntity<UsersRole> getRole(@RequestParam @Valid Long userIdx) {
        log.info("=======================================================");
        log.info("getRole - Role Request");
        log.info("userIdx : {}", userIdx);
        log.info("=======================================================");

        return ResponseEntity.ok(authService.getRole(userIdx));
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(
            @CookieValue(name = CookieUtil.REFRESH_TOKEN_NAME, required = false) String refreshToken,
            HttpServletResponse response) {

        log.info("===== Token Refresh Request =====");

        if (refreshToken == null) {
            return ResponseEntity.status(401).body("Refresh Token이 존재하지 않습니다.");
        }

        try {
            // 1. 서비스 로직 수행
            TokenDto tokenDto = authService.refreshToken(refreshToken);

            // 2. Access Token 쿠키 갱신 (무조건)
            CookieUtil.addCookie(response,
                    CookieUtil.ACCESS_TOKEN_NAME,
                    tokenDto.getAccessToken(),
                    CookieUtil.ACCESS_TOKEN_EXPIRY);

            // 3. Refresh Token 쿠키 갱신 (값이 있을 때만 = 6일 이하로 남아서 갱신된 경우)
            if (tokenDto.getRefreshToken() != null) {
                CookieUtil.addCookie(response,
                        CookieUtil.REFRESH_TOKEN_NAME,
                        tokenDto.getRefreshToken(),
                        CookieUtil.REFRESH_TOKEN_EXPIRY);
                log.info("Refresh Token 쿠키가 갱신되었습니다.");
            }

            return ResponseEntity.ok("토큰이 갱신되었습니다.");

        } catch (IllegalArgumentException e) {
            log.error("토큰 갱신 실패: {}", e.getMessage());
            // 토큰이 유효하지 않으므로 쿠키 삭제 처리 등 추가 가능
            CookieUtil.deleteCookie(response, CookieUtil.ACCESS_TOKEN_NAME);
            CookieUtil.deleteCookie(response, CookieUtil.REFRESH_TOKEN_NAME);
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

}
