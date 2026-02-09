package co.kr.user.controller;

import co.kr.user.model.dto.auth.AuthenticationReq;
import co.kr.user.model.dto.register.RegisterDTO;
import co.kr.user.model.dto.register.RegisterReq;
import co.kr.user.model.dto.register.SignUpCheckReq;
import co.kr.user.service.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 가입(Register) 관련 API 요청을 처리하는 컨트롤러 클래스입니다.
 * 이메일 중복 확인, 회원 가입 신청(정보 저장 및 인증 메일 발송), 최종 가입 인증 등의 기능을 제공합니다.
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
     * 회원 가입 시 사용자가 입력한 이메일(아이디)이 이미 사용 중인지 확인합니다.
     *
     * @param signUpCheckReq HTTP Body에 포함된 중복 확인할 데이터 (이메일)
     * @Valid 어노테이션을 통해 입력 데이터의 유효성을 검증합니다.
     * @return 중복 확인 결과 메시지(사용 가능 여부)를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PostMapping("/signup/check")
    public ResponseEntity<String> duplicateCheck(@RequestBody @Valid SignUpCheckReq signUpCheckReq) {
        // RegisterService를 호출하여 이메일 중복 여부를 검사하고 결과를 반환합니다.
        return ResponseEntity.ok(registerService.checkDuplicate(signUpCheckReq.getEmail()));
    }

    /**
     * 회원 가입 신청 API
     * 신규 사용자의 정보(이름, 비밀번호, 생년월일 등)를 받아 임시 회원으로 등록하고 인증 메일을 발송합니다.
     *
     * @param registerReq HTTP Body에 포함된 회원 가입 요청 정보
     * @Valid 어노테이션을 통해 가입 정보의 유효성을 검증합니다.
     * @return 가입 신청 결과(인증 만료 시간 등)를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PostMapping("/signup")
    public ResponseEntity<RegisterDTO> signup(@RequestBody @Valid RegisterReq registerReq) {
        // RegisterService를 호출하여 회원 정보 저장 및 인증 메일 발송 로직을 수행합니다.
        return ResponseEntity.ok(registerService.signup(registerReq));
    }

    /**
     * 가입 인증 API
     * 이메일로 발송된 인증 코드를 검증하여 회원 가입을 최종 승인(계정 활성화)합니다.
     *
     * @param authenticationReq HTTP Body에 포함된 인증 코드 정보
     * @Valid 어노테이션을 통해 인증 코드 데이터의 유효성을 검증합니다.
     * @return 인증 성공 여부 또는 결과 메시지를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PostMapping("/signup/Authentication")
    public ResponseEntity<String> signupAuthentication(@RequestBody @Valid AuthenticationReq authenticationReq) {
        // RegisterService를 호출하여 인증 코드를 검증하고 계정을 활성화합니다.
        return ResponseEntity.ok(registerService.signupAuthentication(authenticationReq.getCode()));
    }
}