package co.kr.user.controller;

import co.kr.user.model.DTO.retrieve.FindPWFirstStepReq;
import co.kr.user.model.DTO.retrieve.FindPWSecondStepReq;
import co.kr.user.model.DTO.retrieve.FindPWFirstStepDTO;
import co.kr.user.service.RetrieveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 정보 찾기(Retrieve) 관련 API 요청을 처리하는 컨트롤러 클래스입니다.
 * 비밀번호 찾기(재설정) 프로세스 등의 기능을 제공합니다.
 */
@Validated // 요청 데이터의 유효성 검증(Validation) 기능을 활성화합니다.
@RestController // RESTful API 컨트롤러임을 나타내며, 응답 데이터를 JSON 형식으로 반환합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성을 주입받습니다.
@RequestMapping("/auth") // 이 클래스의 API 기본 경로를 "/auth"로 설정합니다.
public class RetrieveController {
    // 회원 정보 찾기 관련 비즈니스 로직을 처리하는 서비스 객체입니다.
    private final RetrieveService retrieveService;

    /**
     * 비밀번호 찾기 1단계 API (회원 정보 확인)
     * 비밀번호를 재설정하기 위해 사용자의 아이디(또는 이메일) 등 정보를 입력받아
     * 해당 회원이 존재하는지 확인하고, 인증 절차를 위한 데이터를 반환합니다.
     *
     * @param findPWFirstStepReq HTTP Body에 포함된 비밀번호 찾기 1단계 요청 데이터 (아이디, 전화번호 등)
     * @Valid 어노테이션을 통해 입력 데이터의 유효성을 검증합니다.
     * @return ResponseEntity<FindPWFirstStepDTO> 1단계 처리 결과(인증 정보 등)를 포함한 응답 객체
     */
    @PostMapping("/findPW/findID")
    public ResponseEntity<FindPWFirstStepDTO> findPW(@RequestBody @Valid FindPWFirstStepReq findPWFirstStepReq) {
        // retrieveService를 통해 비밀번호 찾기 1단계 로직을 수행하고 결과를 반환합니다.
        return ResponseEntity.ok(retrieveService.findPwFirst(findPWFirstStepReq));
    }

    /**
     * 비밀번호 찾기 2단계 API (비밀번호 재설정)
     * 1단계 인증을 마친 사용자가 새로운 비밀번호를 설정하는 요청을 처리합니다.
     *
     * @param findPWSecondStepReq HTTP Body에 포함된 비밀번호 재설정 요청 데이터 (인증 코드, 새 비밀번호 등)
     * @Valid 어노테이션을 통해 입력 데이터의 유효성을 검증합니다.
     * @return ResponseEntity<String> 비밀번호 재설정 완료 메시지 또는 결과를 포함한 응답 객체
     */
    @PostMapping("/findPW/setPW")
    public ResponseEntity<String> findPW(@RequestBody @Valid FindPWSecondStepReq findPWSecondStepReq) {
        // retrieveService를 통해 비밀번호 변경(재설정) 로직을 수행하고 성공 여부(메시지)를 반환합니다.
        return ResponseEntity.ok(retrieveService.findPwSecond(findPWSecondStepReq));
    }
}