package co.kr.user.controller;

import co.kr.user.model.DTO.auth.AuthenticationReq;
import co.kr.user.model.DTO.register.RegisterDTO;
import co.kr.user.model.DTO.register.RegisterReq;
import co.kr.user.model.DTO.register.SignUpCheckReq;
import co.kr.user.service.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 가입(Register) 관련 API 요청을 처리하는 컨트롤러 클래스입니다.
 * 중복 확인, 회원 가입 신청, 가입 인증 등의 기능을 제공합니다.
 */
@Validated // 요청 데이터의 유효성 검증을 활성화합니다.
@RestController // RESTful API 컨트롤러임을 명시하며, 응답 데이터를 JSON으로 반환합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성을 주입받습니다.
@RequestMapping("/auth") // 이 클래스의 API 기본 경로를 "/auth"로 설정합니다.
public class RegisterController {
    // 회원 가입 관련 비즈니스 로직을 처리하는 서비스 객체입니다.
    private final RegisterService registerService;

    /**
     * 중복 확인 API
     * 회원 가입 시 이메일(또는 아이디) 등의 중복 여부를 확인합니다.
     *
     * @param signUpCheckReq HTTP Body에 포함된 중복 확인할 데이터 (이메일 등)
     * @Valid 어노테이션을 통해 입력 데이터의 유효성을 검증합니다.
     * @return ResponseEntity<String> 중복 확인 결과 메시지(사용 가능 여부 등)를 포함한 응답 객체
     */
    @PostMapping("/signup/check")
    public ResponseEntity<String> duplicateCheck(@RequestBody @Valid SignUpCheckReq signUpCheckReq) {
        // registerService를 통해 이메일 중복 검사를 수행하고 결과를 반환합니다.
        return ResponseEntity.ok(registerService.checkDuplicate(signUpCheckReq.getEmail()));
    }

    /**
     * 회원 가입 API
     * 신규 사용자의 정보를 받아 회원으로 등록합니다.
     *
     * @param registerReq HTTP Body에 포함된 회원 가입 요청 정보 (이메일, 비밀번호, 이름 등)
     * @Valid 어노테이션을 통해 가입 정보의 유효성을 검증합니다.
     * @return ResponseEntity<RegisterDTO> 가입 처리 결과 및 등록된 정보를 포함한 응답 객체
     */
    @PostMapping("/signup")
    public ResponseEntity<RegisterDTO> signup(@RequestBody @Valid RegisterReq registerReq) {
        // registerService를 통해 회원 가입 로직을 수행하고 완료된 정보를 반환합니다.
        return ResponseEntity.ok(registerService.signup(registerReq));
    }

    /**
     * 가입 인증 API
     * 회원 가입 절차 중 발송된 인증 코드를 검증하여 가입을 최종 승인하거나 인증 상태를 확인합니다.
     *
     * @param authenticationReq HTTP Body에 포함된 인증 코드 정보
     * @Valid 어노테이션을 통해 인증 코드 데이터의 유효성을 검증합니다.
     * @return ResponseEntity<String> 인증 성공 여부 또는 결과 메시지를 포함한 응답 객체
     */
    @PostMapping("/signup/Authentication")
    public ResponseEntity<String> signupAuthentication(@RequestBody @Valid AuthenticationReq authenticationReq) {
        // registerService를 통해 인증 코드를 확인하고 인증 결과를 반환합니다.
        return ResponseEntity.ok(registerService.signupAuthentication(authenticationReq.getCode()));
    }
}