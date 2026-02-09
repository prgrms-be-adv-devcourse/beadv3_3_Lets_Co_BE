package co.kr.user.controller;

import co.kr.user.model.dto.retrieve.FindPWFirstStepReq;
import co.kr.user.model.dto.retrieve.FindPWSecondStepReq;
import co.kr.user.model.dto.retrieve.FindPWFirstStepDTO;
import co.kr.user.service.RetrieveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 정보 찾기(Retrieve) 관련 API 요청을 처리하는 컨트롤러 클래스입니다.
 * 비밀번호 분실 시 본인 인증 과정을 거쳐 비밀번호를 재설정하는 프로세스를 제공합니다.
 */
@Validated // 데이터 유효성 검증(Validation) 기능을 활성화합니다.
@RestController // RESTful API 컨트롤러임을 나타내며, 응답 데이터를 JSON 형식으로 반환합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성을 주입(DI)받습니다.
@RequestMapping("/auth") // 이 클래스의 API 기본 경로를 "/auth"로 설정합니다 (로그인 전 접근 가능).
public class RetrieveController {

    // 회원 정보 찾기 및 비밀번호 변경 비즈니스 로직을 처리하는 서비스 객체입니다.
    private final RetrieveService retrieveService;

    /**
     * 비밀번호 찾기 1단계 API (회원 정보 확인 및 인증번호 발송)
     * 사용자가 입력한 아이디(이메일)가 시스템에 존재하는지 확인하고,
     * 존재할 경우 등록된 이메일로 본인 인증을 위한 코드를 발송합니다.
     *
     * @param findPWFirstStepReq HTTP Body에 포함된 1단계 요청 데이터 (아이디 등)
     * @Valid 어노테이션을 통해 입력 데이터의 유효성을 검증합니다.
     * @return 1단계 처리 결과(인증 만료 시간 등)를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PostMapping("/findPW/findID")
    public ResponseEntity<FindPWFirstStepDTO> findPW(@RequestBody @Valid FindPWFirstStepReq findPWFirstStepReq) {
        // RetrieveService를 호출하여 인증번호 발송 로직을 수행하고 결과를 반환합니다.
        return ResponseEntity.ok(retrieveService.findPwFirst(findPWFirstStepReq));
    }

    /**
     * 비밀번호 찾기 2단계 API (인증 확인 및 비밀번호 재설정)
     * 이메일로 전송된 인증 코드를 검증하고, 유효한 경우 사용자가 입력한 새 비밀번호로 변경합니다.
     *
     * @param findPWSecondStepReq HTTP Body에 포함된 2단계 요청 데이터 (인증 코드, 새 비밀번호 등)
     * @Valid 어노테이션을 통해 입력 데이터의 유효성을 검증합니다.
     * @return 비밀번호 재설정 완료 메시지를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PostMapping("/findPW/setPW")
    public ResponseEntity<String> findPW(@RequestBody @Valid FindPWSecondStepReq findPWSecondStepReq) {
        // RetrieveService를 호출하여 인증 코드 검증 및 비밀번호 변경 로직을 수행합니다.
        return ResponseEntity.ok(retrieveService.findPwSecond(findPWSecondStepReq));
    }
}