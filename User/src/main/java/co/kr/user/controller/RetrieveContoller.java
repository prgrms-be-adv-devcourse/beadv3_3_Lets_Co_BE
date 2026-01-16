package co.kr.user.controller;

import co.kr.user.model.DTO.retrieve.FindPWFirstStepReq;
import co.kr.user.model.DTO.retrieve.FindPWSecondStepReq;
import co.kr.user.model.DTO.retrieve.FindPWFirstStepDTO;
import co.kr.user.service.RetrieveService;
import jakarta.validation.Valid;
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
public class RetrieveContoller {

    private final RetrieveService retrieveService;

    @PostMapping("/findPW/findID")
    public ResponseEntity<FindPWFirstStepDTO> findPW(@RequestBody @Valid FindPWFirstStepReq findPWFirstStepReq) {
        log.info("=======================================================");
        log.info("findPW - Find Password First Step");
        log.info("ID : {}", findPWFirstStepReq.toString());
        log.info("=======================================================");

        return ResponseEntity.ok(retrieveService.findPwFirst(findPWFirstStepReq));
    }

    @PostMapping("/findPW/setPW")
    public ResponseEntity<String> findPW(@RequestBody @Valid FindPWSecondStepReq findPWSecondStepReq) {
        log.info("=======================================================");
        log.info("findPW - Find Password Second Step");
        log.info("RetrieveSecondReq : {}", findPWSecondStepReq.toString());
        log.info("=======================================================");

        return ResponseEntity.ok(retrieveService.findPwSecond(findPWSecondStepReq));
    }

}
