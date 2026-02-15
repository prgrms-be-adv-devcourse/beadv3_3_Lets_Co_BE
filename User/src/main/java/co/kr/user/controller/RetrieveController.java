package co.kr.user.controller;

import co.kr.user.model.dto.retrieve.*;
import co.kr.user.service.RetrieveService;
import co.kr.user.util.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 아이디 및 비밀번호 찾기(회수) 프로세스를 처리하는 컨트롤러입니다.
 * 인증 메일 발송 및 코드 검증을 통한 2단계 프로세스를 제공합니다.
 * 이 컨트롤러의 기능은 로그인 전 사용자가 접근 가능해야 하므로 인증 토큰 없이 호출될 수 있어야 합니다.
 */
@Validated // 요청 본문의 유효성 검증을 활성화합니다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth") // 인증 관련 기능이므로 "/auth" 경로 하위에 위치합니다.
public class RetrieveController {
    private final RetrieveService retrieveService;

    /**
     * [아이디 찾기 1단계]
     * 사용자가 입력한 이메일로 가입 여부를 확인하고, 인증 메일을 발송합니다.
     * @param findIDFirstStepReq 이메일 정보가 담긴 요청 객체
     * @return 1단계 결과 DTO (이메일, 인증 만료 시간)를 담은 응답 객체
     */
    @PostMapping("/findID/findMail")
    public ResponseEntity<BaseResponse<FindIDFirstStepDTO>> findID(@RequestBody @Valid FindIDFirstStepReq findIDFirstStepReq) {
        // 서비스 계층을 호출하여 이메일 확인 및 인증 메일 발송 수행
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", retrieveService.findIdFirst(findIDFirstStepReq.getMail())));
    }

    /**
     * [아이디 찾기 2단계]
     * 이메일로 전송된 인증 코드를 검증하고, 성공 시 아이디를 반환합니다.
     * @param findIDSecondStepReq 이메일과 인증 코드가 담긴 요청 객체
     * @return 찾은 사용자 아이디를 담은 응답 객체
     */
    @PostMapping("/findID/getID")
    public ResponseEntity<BaseResponse<String>> findID(@RequestBody @Valid FindIDSecondStepReq findIDSecondStepReq) {
        // 서비스 계층을 호출하여 코드 검증 후 아이디 반환
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", retrieveService.findIdSecond(findIDSecondStepReq)));
    }

    /**
     * [비밀번호 찾기 1단계]
     * 사용자가 입력한 이메일로 가입 여부를 확인하고, 비밀번호 재설정용 인증 메일을 발송합니다.
     * @param findPWFirstStepReq 이메일 정보가 담긴 요청 객체
     * @return 1단계 결과 DTO (이메일, 인증 만료 시간)를 담은 응답 객체
     */
    @PostMapping("/findPW/findMail")
    public ResponseEntity<BaseResponse<FindPWFirstStepDTO>> findPW(@RequestBody @Valid FindPWFirstStepReq findPWFirstStepReq) {
        // 서비스 계층을 호출하여 이메일 확인 및 인증 메일 발송 수행
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", retrieveService.findPwFirst(findPWFirstStepReq.getMail())));
    }

    /**
     * [비밀번호 찾기 2단계]
     * 인증 코드를 검증하고, 새로운 비밀번호로 변경합니다.
     * @param findPWSecondStepReq 이메일, 인증 코드, 새 비밀번호가 담긴 요청 객체
     * @return 처리 성공 메시지를 담은 응답 객체
     */
    @PostMapping("/findPW/setPW")
    public ResponseEntity<BaseResponse<String>> findPW(@RequestBody @Valid FindPWSecondStepReq findPWSecondStepReq) {
        // 서비스 계층을 호출하여 코드 검증 및 비밀번호 변경 수행
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", retrieveService.findPwSecond(findPWSecondStepReq)));
    }
}