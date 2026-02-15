package co.kr.user.controller;

import co.kr.user.model.dto.register.AuthenticationReq;
import co.kr.user.model.dto.register.RegisterDTO;
import co.kr.user.model.dto.register.RegisterReq;
import co.kr.user.model.dto.register.SignUpCheckReq;
import co.kr.user.service.RegisterService;
import co.kr.user.util.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 회원가입 프로세스를 처리하는 컨트롤러입니다.
 * 아이디 중복 확인, 가입 요청, 이메일 인증 확인 등의 단계를 수행합니다.
 */
@Validated // 데이터 유효성 검증을 위한 어노테이션입니다.
@RestController // RESTful API 처리를 위한 컨트롤러입니다.
@RequiredArgsConstructor // 생성자 주입을 자동으로 처리합니다.
@RequestMapping("/auth") // 회원가입은 인증 관련이므로 "/auth" 경로 하위에 위치합니다.
public class RegisterController {
    // 회원가입 비즈니스 로직을 담당하는 서비스 주입
    private final RegisterService registerService;

    /**
     * 회원가입 시 아이디 중복 여부를 확인합니다.
     * * @param signUpCheckReq 중복 확인할 아이디가 담긴 요청 객체
     * @return 중복 여부에 따른 메시지 ("사용 가능한 아이디입니다" 등)
     */
    @PostMapping("/signup/check")
    public ResponseEntity<BaseResponse<String>> duplicateCheck(@RequestBody @Valid SignUpCheckReq signUpCheckReq) {
        // 서비스 계층의 중복 확인 메서드 호출
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", registerService.checkDuplicate(signUpCheckReq.getId())));
    }

    /**
     * 회원가입을 요청합니다.
     * 사용자 정보를 임시 저장(대기 상태)하고, 이메일 인증 메일을 발송합니다.
     * * @param registerReq 회원가입에 필요한 정보 (아이디, 비밀번호, 이메일 등)
     * @return 가입 진행 결과 (인증 메일 발송 정보 등)
     */
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<RegisterDTO>> signup(@RequestBody @Valid RegisterReq registerReq) {
        // 서비스 계층을 통해 가입 요청 처리 및 인증 메일 발송
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", registerService.signup(registerReq)));
    }

    /**
     * 이메일로 전송된 인증 코드를 확인하여 회원가입을 최종 완료합니다.
     * * @param authenticationReq 사용자가 입력한 인증 코드
     * @return 인증 완료 메시지
     */
    @PostMapping("/signup/Authentication")
    public ResponseEntity<BaseResponse<String>> signupAuthentication(@RequestBody @Valid AuthenticationReq authenticationReq) {
        // 서비스 계층을 통해 인증 코드 검증 및 계정 활성화 처리
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", registerService.signupAuthentication(authenticationReq.getAuthCode())));
    }
}