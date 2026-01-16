package co.kr.user.controller;

import co.kr.user.model.DTO.login.LoginDTO;
import co.kr.user.model.DTO.login.LoginReq;
import co.kr.user.service.LoginService;
import co.kr.user.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<LoginDTO> login(@RequestBody @Valid LoginReq loginReq, HttpServletResponse response) {
        log.info("===== login - Login Request =====");

        // 1. 서비스 로직 수행
        LoginDTO loginDTO = loginService.login(loginReq);

        // 2. [Refactoring] CookieUtil을 사용하여 Refresh Token 저장
        // (HttpOnly, Secure, Path, MaxAge 설정이 Util 내부에서 자동 처리됨)
        CookieUtil.addCookie(response,
                CookieUtil.REFRESH_TOKEN_NAME,
                loginDTO.getRefreshToken(),
                CookieUtil.REFRESH_TOKEN_EXPIRY);

        // 3. Response Body에서 Refresh Token 제거 (보안)
        loginDTO.setRefreshToken(null);

        return ResponseEntity.ok(loginDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            // 쿠키 값을 가져올 때 상수를 사용 (@CookieValue의 name 속성에는 상수 직접 사용 불가하여 문자열 유지하거나, HttpServletRequest로 변경 가능)
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {

        log.info("===== logout - Logout Request =====");

        // 1. 서비스 로그아웃 처리
        String value = loginService.logout(refreshToken);

        // 2. [Refactoring] CookieUtil을 사용하여 쿠키 삭제
        CookieUtil.deleteCookie(response, CookieUtil.ACCESS_TOKEN_NAME);  // 혹시 있을 Access Token 삭제
        CookieUtil.deleteCookie(response, CookieUtil.REFRESH_TOKEN_NAME); // Refresh Token 삭제

        return ResponseEntity.ok(value);
    }
}