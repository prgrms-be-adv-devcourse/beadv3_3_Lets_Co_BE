package co.kr.user.controller;

import co.kr.user.model.DTO.login.LoginDTO;
import co.kr.user.model.DTO.login.LoginReq;
import co.kr.user.service.LoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j // Lombok: 로그를 남기기 위한 log 객체를 자동으로 생성해줍니다.
@Validated // Controller 레벨에서 @RequestParam 등의 유효성 검증(Validation)을 활성화합니다.
@RestController // 이 클래스가 REST API용 컨트롤러임을 명시 (모든 메서드의 리턴값이 JSON 형태가 됨)
@RequiredArgsConstructor // final이 붙은 필드에 대해 생성자를 자동으로 만들어주어 의존성을 주입받습니다.
@RequestMapping("/auth") // 이 컨트롤러의 기본 URL 경로를 '/auth'로 설정합니다. (예: /auth/signup)
public class LoginController {

    private  final LoginService loginService;



    @PostMapping("/login")
    public ResponseEntity<LoginDTO> login(@RequestBody @Valid LoginReq loginReq) {
        log.info("=======================================================");
        log.info("login - Login Request");
        log.info("loginReq : {}", loginReq);
        log.info("=======================================================");

        return ResponseEntity.ok(loginService.login(loginReq));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(// 쿠키에서 refresh_token을 가져옴 (없을 수도 있으니 required = false)
                                         @CookieValue(name = "refresh_token", required = false) String refreshToken,
                                         HttpServletResponse response) {
        log.info("=======================================================");
        log.info("logout - Logout Request");
        log.info("refreshToken : {}", refreshToken);
        log.info("=======================================================");

        String value = loginService.logout(refreshToken);

        // Access Token 삭제
        Cookie accessCookie = new Cookie("access_token", null); // 값은 null
        accessCookie.setMaxAge(0); // 수명 0초 (즉시 삭제)
        accessCookie.setPath("/"); // 발급할 때와 동일한 경로여야 지워짐
        response.addCookie(accessCookie);

        // Refresh Token 삭제
        Cookie refreshCookie = new Cookie("refresh_token", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true); // 발급 때 설정과 동일하게
        // refreshCookie.setSecure(true); // HTTPS 사용 시 주석 해제
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(value);
    }
}
