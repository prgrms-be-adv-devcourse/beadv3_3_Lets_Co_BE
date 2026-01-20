package co.kr.user.controller;

import co.kr.user.model.DTO.retrieve.RetrieveFirstDTO;
import co.kr.user.model.DTO.retrieve.RetrieveSecondReq;
import co.kr.user.model.DTO.retrieve.RetrieveThirdReq;
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

    @PostMapping("/find-pw-1")
    public ResponseEntity<RetrieveFirstDTO> findPwFirst(@RequestParam @Valid String ID) {
        log.info("=======================================================");
        log.info("findPwFirst - Find Password First Request");
        log.info("ID : {}", ID);
        log.info("=======================================================");

        return ResponseEntity.ok(retrieveService.findPwFirst(ID));
    }

    @PostMapping("/find-pw-2")
    public ResponseEntity<String> findPwSecond(@RequestBody @Valid RetrieveSecondReq retrieveSecondReq) {
        log.info("=======================================================");
        log.info("findPwSecond - Find Password Second Request");
        log.info("RetrieveSecondReq : {}", retrieveSecondReq.toString());
        log.info("=======================================================");

        return ResponseEntity.ok(retrieveService.findPwSecond(retrieveSecondReq));
    }

    @PostMapping("/find-pw-3")
    public ResponseEntity<String> findPwThird(@RequestBody @Valid RetrieveThirdReq retrieveThirdReq) {
        log.info("=======================================================");
        log.info("findPwThird - Find Password Third Request");
        log.info("RetrieveThirdReq : {}", retrieveThirdReq.toString());
        log.info("=======================================================");

        return ResponseEntity.ok(retrieveService.findPwThird(retrieveThirdReq));
    }

}
