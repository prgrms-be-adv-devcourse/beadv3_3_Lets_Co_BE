package co.kr.user.controller;

import co.kr.user.model.dto.login.LoginDTO;
import co.kr.user.model.dto.login.LoginReq;
import co.kr.user.service.LoginService;
import co.kr.user.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 로그인(Login) 및 로그아웃(Logout)과 관련된 API 요청을 처리하는 컨트롤러 클래스입니다.
 * 사용자의 인증 절차를 수행하고, 인증 성공 시 JWT 토큰(Access Token, Refresh Token)을 발급하여
 * 클라이언트의 쿠키(Cookie)에 안전하게 저장하거나, 로그아웃 시 이를 삭제하는 역할을 담당합니다.
 */
@Validated // 요청 데이터(파라미터, 바디 등)의 유효성 검증(Validation) 기능을 활성화합니다.
@RestController // RESTful API 컨트롤러임을 명시하며, 리턴값을 자동으로 JSON 형식으로 변환하여 응답합니다.
@RequiredArgsConstructor // final로 선언된 필드(loginService)에 대한 생성자를 자동으로 생성하여 의존성을 주입(DI)받습니다.
@RequestMapping("/auth") // 이 클래스 내의 모든 API 엔드포인트 URL은 "/auth"로 시작하도록 설정합니다.
public class LoginController {

    // 로그인 및 로그아웃에 대한 비즈니스 로직을 수행하는 서비스 객체입니다.
    private final LoginService loginService;

    /**
     * 로그인 API입니다.
     * 클라이언트로부터 아이디와 비밀번호를 받아 인증을 수행하고, 성공 시 JWT 토큰을 발급하여 쿠키에 설정합니다.
     *
     * @param loginReq HTTP Request Body에 포함된 로그인 요청 데이터 (아이디, 비밀번호)입니다. @Valid로 유효성을 검증합니다.
     * @param response HTTP Response 객체입니다. 발급된 토큰을 쿠키에 담아 클라이언트로 보내기 위해 사용합니다.
     * @return 로그인 성공 메시지를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginReq loginReq, HttpServletResponse response) {
        // 1. LoginService를 호출하여 사용자 인증을 수행합니다.
        //    인증에 성공하면 Access Token과 Refresh Token이 담긴 LoginDTO를 반환받습니다.
        LoginDTO loginDTO = loginService.login(loginReq);

        // 2. 발급받은 Refresh Token을 HttpOnly 쿠키에 설정합니다.
        //    HttpOnly 설정은 자바스크립트를 통한 접근을 차단하여 XSS 공격을 예방합니다.
        CookieUtil.addCookie(response,
                CookieUtil.REFRESH_TOKEN_NAME,   // 쿠키 이름 (예: refresh_token)
                loginDTO.getRefreshToken(),      // 쿠키 값 (토큰 문자열)
                CookieUtil.REFRESH_TOKEN_EXPIRY  // 쿠키 유효 시간
        );

        // 3. 발급받은 Access Token을 HttpOnly 쿠키에 설정합니다.
        //    API 요청 시 인증 수단으로 사용됩니다.
        CookieUtil.addCookie(response,
                CookieUtil.ACCESS_TOKEN_NAME,    // 쿠키 이름 (예: access_token)
                loginDTO.getAccessToken(),       // 쿠키 값 (토큰 문자열)
                CookieUtil.ACCESS_TOKEN_EXPIRY   // 쿠키 유효 시간
        );

        // 4. 모든 처리가 정상적으로 완료되면 클라이언트에게 성공 메시지를 반환합니다.
        return ResponseEntity.ok("로그인 성공");
    }

    /**
     * 로그아웃 API입니다.
     * 서버 측에서 리프레시 토큰을 만료 처리하고, 클라이언트 브라우저에 저장된 토큰 쿠키를 삭제합니다.
     *
     * @param refreshToken 클라이언트 쿠키에서 추출한 Refresh Token 값입니다. (@CookieValue 사용)
     * (required = false)로 설정하여 쿠키가 없더라도 로그아웃 로직이 진행되도록 합니다.
     * @param response HTTP Response 객체입니다. 클라이언트의 쿠키를 삭제(만료)시키기 위해 사용합니다.
     * @return 로그아웃 처리 결과를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                         HttpServletResponse response) {
        // 1. LoginService를 호출하여 서버 데이터베이스에 저장된 리프레시 토큰을 만료/삭제 처리합니다.
        //    토큰이 유효하지 않거나 존재하지 않아도 로그아웃 프로세스는 계속 진행됩니다.
        String value = loginService.logout(refreshToken);

        // 2. 클라이언트 브라우저에 저장된 Access Token 쿠키를 삭제합니다.
        //    (유효 시간을 0으로 설정한 쿠키를 덮어씌워 삭제 효과를 냅니다.)
        CookieUtil.deleteCookie(response, CookieUtil.ACCESS_TOKEN_NAME);
        // 3. 클라이언트 브라우저에 저장된 Refresh Token 쿠키를 삭제합니다.
        CookieUtil.deleteCookie(response, CookieUtil.REFRESH_TOKEN_NAME);

        // 4. 로그아웃 처리가 완료되었음을 알리는 메시지를 반환합니다.
        return ResponseEntity.ok(value);
    }
}