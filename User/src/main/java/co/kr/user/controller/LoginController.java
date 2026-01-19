package co.kr.user.controller;

import co.kr.user.model.DTO.login.LoginDTO;
import co.kr.user.model.DTO.login.LoginReq;
import co.kr.user.service.LoginService;
import co.kr.user.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 로그인(Login) 및 로그아웃 관련 API 요청을 처리하는 컨트롤러 클래스입니다.
 * 사용자의 인증을 수행하고, JWT 토큰을 쿠키(Cookie)에 저장하거나 삭제하는 기능을 제공합니다.
 */
@Validated // 요청 파라미터나 바디의 데이터 유효성 검증을 활성화하는 어노테이션입니다.
@RestController // RESTful API 컨트롤러임을 명시하며, 응답 데이터를 JSON 형식(또는 String)으로 반환합니다.
@RequiredArgsConstructor // final로 선언된 필드(loginService)에 대한 생성자를 자동으로 생성하여 의존성을 주입받습니다.
@RequestMapping("/auth") // 이 클래스 내의 모든 API 경로는 "/auth"로 시작합니다.
public class LoginController {
    // 로그인 및 로그아웃 비즈니스 로직을 처리하는 서비스 객체입니다.
    private final LoginService loginService;

    /**
     * 로그인 API
     * 사용자의 아이디와 비밀번호를 받아 인증을 수행하고, 성공 시 액세스 토큰과 리프레시 토큰을 쿠키에 설정합니다.
     *
     * @param loginReq HTTP Body에 포함된 로그인 요청 데이터 (이메일, 비밀번호 등)
     * @param response HTTP 응답 객체 (쿠키를 추가하기 위해 사용)
     * @Valid 어노테이션을 통해 로그인 요청 데이터의 유효성을 검증합니다.
     * @return ResponseEntity<String> 로그인 성공 메시지를 포함한 응답 객체
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginReq loginReq, HttpServletResponse response) {
        // 1. 서비스 로직을 호출하여 사용자 인증을 수행하고, 생성된 토큰 정보(LoginDTO)를 받아옵니다.
        LoginDTO loginDTO = loginService.login(loginReq);

        // 2. 리프레시 토큰(Refresh Token)을 쿠키에 추가합니다.
        // CookieUtil을 사용하여 보안 설정(HttpOnly 등)이 적용된 쿠키를 생성합니다.
        CookieUtil.addCookie(response,
                CookieUtil.REFRESH_TOKEN_NAME,   // 쿠키 이름 (예: refresh_token)
                loginDTO.getRefreshToken(),      // 쿠키 값 (토큰 문자열)
                CookieUtil.REFRESH_TOKEN_EXPIRY  // 쿠키 만료 시간
        );

        // 3. 액세스 토큰(Access Token)을 쿠키에 추가합니다.
        CookieUtil.addCookie(response,
                CookieUtil.ACCESS_TOKEN_NAME,    // 쿠키 이름 (예: access_token)
                loginDTO.getAccessToken(),       // 쿠키 값 (토큰 문자열)
                CookieUtil.ACCESS_TOKEN_EXPIRY   // 쿠키 만료 시간
        );

        // 4. 모든 처리가 완료되면 로그인 성공 메시지를 반환합니다.
        return ResponseEntity.ok("로그인 성공");
    }

    /**
     * 로그아웃 API
     * 쿠키에 저장된 리프레시 토큰을 사용하여 로그아웃 로직을 수행하고, 클라이언트의 토큰 쿠키를 삭제합니다.
     *
     * @param refreshToken 쿠키에서 추출한 리프레시 토큰 값 (@CookieValue 사용, 없을 경우 null 허용)
     * @param response HTTP 응답 객체 (쿠키를 삭제하기 위해 사용)
     * @return ResponseEntity<String> 로그아웃 처리 결과 메시지를 포함한 응답 객체
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                         HttpServletResponse response) {
        // 1. 서비스 로직을 호출하여 서버 측 로그아웃 처리(예: Redis에서 토큰 삭제 등)를 수행합니다.
        // 리프레시 토큰이 쿠키에 없더라도(null) 로그아웃 프로세스는 진행됩니다.
        String value = loginService.logout(refreshToken);

        // 2. 클라이언트 브라우저에 저장된 액세스 토큰 쿠키를 삭제합니다. (만료 시간을 0으로 설정하여 덮어씌움)
        CookieUtil.deleteCookie(response, CookieUtil.ACCESS_TOKEN_NAME);
        // 3. 클라이언트 브라우저에 저장된 리프레시 토큰 쿠키를 삭제합니다.
        CookieUtil.deleteCookie(response, CookieUtil.REFRESH_TOKEN_NAME);

        // 4. 로그아웃 처리 결과를 반환합니다.
        return ResponseEntity.ok(value);
    }
}