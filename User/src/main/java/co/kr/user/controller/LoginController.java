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

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<LoginDTO> login(@RequestBody @Valid LoginReq loginReq, HttpServletResponse response) {
        log.info("=======================================================");
        log.info("login - Login Request");
        log.info("loginReq : {}", loginReq.toString());
        log.info("=======================================================");

        // 1. 서비스 로직 수행 (Access + Refresh Token 발급)
        LoginDTO loginDTO = loginService.login(loginReq);

        // 2. Refresh Token을 HttpOnly 쿠키로 생성
        // (XSS 공격으로부터 토큰을 보호하기 위함)
        Cookie refreshCookie = new Cookie("refresh_token", loginDTO.getRefreshToken());
        refreshCookie.setHttpOnly(true);          // JS에서 접근 불가 (핵심)
        refreshCookie.setPath("/");               // 사이트 전역에서 쿠키 사용
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일 (초 단위) - Refresh Token 만료 시간과 비슷하게 설정
        // refreshCookie.setSecure(true);         // HTTPS 환경에서만 전송 (운영 배포 시 주석 해제 권장)

        // 3. 응답(Response)에 쿠키 추가
        response.addCookie(refreshCookie);

        // 4. [보안 강화] Response Body(JSON)에서는 Refresh Token 제거
        // 이미 쿠키에 안전하게 담았으므로, JSON 데이터에 중복해서 노출할 필요가 없음.
        // 이를 통해 클라이언트(JS)가 실수로 로컬 스토리지 등에 저장하는 것을 원천 차단함.
        loginDTO.setRefreshToken(null);

        return ResponseEntity.ok(loginDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(// 쿠키에서 refresh_token을 가져옴 (없을 수도 있으니 required = false)
                                         @CookieValue(name = "refresh_token", required = false) String refreshToken,
                                         HttpServletResponse response) {
        log.info("=======================================================");
        log.info("logout - Logout Request");
        log.info("refreshToken : {}", refreshToken);
        log.info("=======================================================");

        // 1. 서비스 로그아웃 처리 (DB 상태 변경 등)
        String value = loginService.logout(refreshToken);

        // 2. Access Token 쿠키 삭제 (혹시 저장했다면)
        Cookie accessCookie = new Cookie("access_token", null);
        accessCookie.setMaxAge(0); // 수명 0초 (즉시 삭제)
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        // 3. Refresh Token 쿠키 삭제
        Cookie refreshCookie = new Cookie("refresh_token", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        // refreshCookie.setSecure(true); // HTTPS 사용 시 주석 해제
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(value);
    }
}