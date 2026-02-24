package co.kr.user.controller;

import co.kr.user.model.dto.login.LoginDTO;
import co.kr.user.model.dto.login.LoginReq;
import co.kr.user.service.LoginService;
import co.kr.user.util.BaseResponse;
import co.kr.user.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 로그인 및 로그아웃과 관련된 요청을 처리하는 컨트롤러입니다.
 * 사용자의 인증을 수행하고, JWT 토큰(Access/Refresh)을 쿠키에 설정하거나 삭제합니다.
 */
@Validated // 데이터 유효성 검증(@Valid 등) 기능을 활성화합니다.
@RestController // JSON 형태의 응답을 반환하는 REST 컨트롤러임을 명시합니다.
@RequiredArgsConstructor // final로 선언된 필드에 대해 생성자를 자동으로 생성합니다.
@RequestMapping("/auth") // 이 컨트롤러의 모든 API 경로는 "/auth"로 시작합니다.
public class LoginController {
    // 로그인 비즈니스 로직을 처리하는 서비스 주입
    private final LoginService loginService;

    /**
     * 로그인 요청을 처리합니다.
     * 아이디와 비밀번호를 검증하고, 성공 시 JWT 토큰을 발급하여 쿠키에 저장합니다.
     * * @param loginReq 사용자가 입력한 로그인 정보 (아이디, 비밀번호)
     * @param response 쿠키 설정을 위한 HttpServletResponse 객체
     * @return 로그인 성공 메시지
     */
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<String>> login(@RequestBody @Valid LoginReq loginReq,
                                                      HttpServletResponse response) {
        // 서비스 계층을 통해 로그인 로직 수행 (인증 및 토큰 발급)
        LoginDTO loginDTO = loginService.login(loginReq);

        // 발급받은 Refresh Token을 쿠키에 추가 (HttpOnly, 만료시간 설정)
        CookieUtil.addCookie(response,
                CookieUtil.REFRESH_TOKEN_NAME,
                loginDTO.getRefreshToken(),
                CookieUtil.REFRESH_TOKEN_EXPIRY
        );

        // 발급받은 Access Token을 쿠키에 추가 (HttpOnly, 만료시간 설정)
        CookieUtil.addCookie(response,
                CookieUtil.ACCESS_TOKEN_NAME,
                loginDTO.getAccessToken(),
                CookieUtil.ACCESS_TOKEN_EXPIRY
        );

        // 성공 응답 반환
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", "로그인 성공"));
    }

    /**
     * 로그아웃 요청을 처리합니다.
     * 클라이언트의 쿠키를 삭제하고, 서버 측(Redis)에서도 토큰을 무효화합니다.
     * * @param refreshToken 쿠키에서 추출한 Refresh Token (없을 경우 null)
     * @param response 쿠키 삭제를 위한 HttpServletResponse 객체
     * @return 로그아웃 성공 메시지
     */
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<String>> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                                       HttpServletResponse response) {

        // 클라이언트 브라우저의 Access Token 쿠키 삭제 (만료 시간을 0으로 설정)
        CookieUtil.deleteCookie(response, CookieUtil.ACCESS_TOKEN_NAME);
        // 클라이언트 브라우저의 Refresh Token 쿠키 삭제
        CookieUtil.deleteCookie(response, CookieUtil.REFRESH_TOKEN_NAME);
        //클라이언트 브라우저의 Chat Token 쿠키 삭제
        CookieUtil.deleteCookie(response, CookieUtil.CHAT_TOKEN_NAME);

        // 서비스 계층을 호출하여 Redis 등에서 토큰 정보를 삭제하거나 블랙리스트 처리
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", loginService.logout(refreshToken)));
    }
}