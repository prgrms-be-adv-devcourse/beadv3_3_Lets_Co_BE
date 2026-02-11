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

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<String>> login(@RequestBody @Valid LoginReq loginReq,
                                                      HttpServletResponse response) {
        LoginDTO loginDTO = loginService.login(loginReq);

        CookieUtil.addCookie(response,
                CookieUtil.REFRESH_TOKEN_NAME,
                loginDTO.getRefreshToken(),
                CookieUtil.REFRESH_TOKEN_EXPIRY
        );

        CookieUtil.addCookie(response,
                CookieUtil.ACCESS_TOKEN_NAME,
                loginDTO.getAccessToken(),
                CookieUtil.ACCESS_TOKEN_EXPIRY
        );

        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", "로그인 성공"));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<String>> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                                       HttpServletResponse response) {

        CookieUtil.deleteCookie(response, CookieUtil.ACCESS_TOKEN_NAME);
        CookieUtil.deleteCookie(response, CookieUtil.REFRESH_TOKEN_NAME);

        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", loginService.logout(refreshToken)));
    }
}