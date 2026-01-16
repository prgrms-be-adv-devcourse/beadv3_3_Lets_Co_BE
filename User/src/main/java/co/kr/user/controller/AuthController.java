package co.kr.user.controller;

import co.kr.user.model.vo.UsersRole;
import co.kr.user.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class AuthController {

    private final AuthService authService;

    @GetMapping("/role")
    public ResponseEntity<UsersRole> getRole(@RequestParam @Valid Long userIdx) {
        log.info("=======================================================");
        log.info("getRole - Role Request");
        log.info("userIdx : {}", userIdx);
        log.info("=======================================================");

        return ResponseEntity.ok(authService.getRole(userIdx));
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> renewAccess(@RequestParam @Valid
                                              @NotBlank(message = "리프레시 토큰을 입력해주세요.")  // 빈 값("")이나 null, 공백(" ")을 허용하지 않음
                                              String refreshToken) {
        log.info("=======================================================");
        log.info("renewAccess - Refresh Token Request");
        log.info("renewAccess : {}", refreshToken);
        log.info("=======================================================");

        return ResponseEntity.ok(authService.renewAccessToken(refreshToken));
    }

    @PostMapping("renewRefresh")
    public ResponseEntity<String> renewRefresh(@RequestParam @Valid
                                               @NotBlank(message = "리프레시 토큰을 입력해주세요.")  // 빈 값("")이나 null, 공백(" ")을 허용하지 않음
                                               String refreshToken) {
        log.info("=======================================================");
        log.info("renewRefresh - Refresh Token Request");
        log.info("renewRefresh : {}", refreshToken);
        log.info("=======================================================");

        return ResponseEntity.ok(authService.renewRefreshToken(refreshToken));
    }

}
