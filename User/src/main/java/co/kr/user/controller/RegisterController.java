package co.kr.user.controller;

import co.kr.user.model.DTO.auth.AuthenticationReq;
import co.kr.user.model.DTO.register.RegisterDTO;
import co.kr.user.model.DTO.register.RegisterReq;
import co.kr.user.model.DTO.register.SignUpCheckReq;
import co.kr.user.service.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * [회원가입 컨트롤러]
 * 클라이언트의 회원가입 관련 HTTP 요청을 처리하는 진입점입니다.
 * 입력값에 대한 1차적인 유효성 검증(Validation)을 수행하고, Service 계층의 로직을 호출합니다.
 */
@Slf4j // Lombok: 로그를 남기기 위한 log 객체를 자동으로 생성해줍니다.
@Validated // Controller 레벨에서 @RequestParam 등의 유효성 검증(Validation)을 활성화합니다.
@RestController // 이 클래스가 REST API용 컨트롤러임을 명시 (모든 메서드의 리턴값이 JSON 형태가 됨)
@RequiredArgsConstructor // final이 붙은 필드에 대해 생성자를 자동으로 만들어주어 의존성을 주입받습니다.
@RequestMapping("/auth") // 이 컨트롤러의 기본 URL 경로를 '/auth'로 설정합니다. (예: /auth/signup)
public class RegisterController {

    // 회원가입 비즈니스 로직을 처리하는 서비스 객체 (생성자 주입 방식)
    private final RegisterService registerService;


    /**
     * [이메일 중복 확인 API]
     * 사용자가 입력한 이메일이 이미 가입된 이메일인지 확인합니다.
     * GET 요청: /auth/duplicate-check?email=test@example.com
     *
     * @param signUpCheckReq 검사할 이메일 주소 (필수 입력, 이메일 형식 준수)
     * @return 중복 여부에 따른 메시지 문자열
     */
    @PostMapping("/signup/check")
    public ResponseEntity<String> duplicateCheck(@RequestBody @Valid SignUpCheckReq signUpCheckReq) {

        // [로그 기록] 요청이 들어왔음을 확인하고 파라미터를 남깁니다.
        log.info("=======================================================");
        log.info("duplicate-check - Checking if E-Mail is available");
        log.info("email : {}", signUpCheckReq.getEmail());
        log.info("=======================================================");

        // Service의 checkDuplicate 메서드를 호출하여 결과를 받아온 후, 200 OK 상태코드와 함께 반환합니다.
        return ResponseEntity.ok(registerService.checkDuplicate(signUpCheckReq.getEmail()));
    }

    /**
     * [회원가입 요청 API]
     * 사용자가 입력한 회원 정보를 받아 회원가입을 진행합니다.
     * POST 요청: /auth/signup
     * Body: JSON 데이터 (RegisterReq 구조)
     *
     * @param registerReq 회원가입 요청 DTO (ID, PW, 이름, 연락처 등 포함)
     * @Valid: DTO 내부에 설정된 제약조건(@NotBlank, @Pattern 등)을 검사합니다.
     * @return 가입된 회원의 기본 정보 및 인증 만료 시간 등을 담은 RegisterDTO
     */
    @PostMapping("/signup")
    public ResponseEntity<RegisterDTO> signup(@RequestBody @Valid RegisterReq registerReq) {

        // [로그 기록] 회원가입 요청 데이터 확인 (운영 환경에서는 비밀번호 로그 노출 주의)
        log.info("=======================================================");
        log.info("signup - Register Request");
        log.info("registerReq : {}", registerReq.toString());
        log.info("=======================================================");

        // Service의 signup 메서드를 호출하여 회원가입 로직(DB 저장, 이메일 발송 등)을 수행합니다.
        return ResponseEntity.ok(registerService.signup(registerReq));
    }

    /**
     * [이메일 인증 코드 검증 API]
     * 이메일로 발송된 인증 코드를 사용자가 입력하면, 유효한 코드인지 확인합니다.
     * POST 요청: /auth/signup/Authentication?code=AbCd123
     *
     * @param authenticationReq 사용자가 입력한 인증 코드 문자열
     * @return 인증 성공/실패 여부 메시지
     */
    @PostMapping("/signup/Authentication")
    public ResponseEntity<String> signupAuthentication(@RequestBody @Valid AuthenticationReq authenticationReq) {

        // [로그 기록] 인증 시도 로그
        log.info("=======================================================");
        log.info("signupAuthentication - Authentication Request");
        log.info("code : {}", authenticationReq.getCode());
        log.info("=======================================================");

        // Service의 인증 로직을 호출하여 결과를 반환합니다.
        return ResponseEntity.ok(registerService.signupAuthentication(authenticationReq.getCode()));
    }

}