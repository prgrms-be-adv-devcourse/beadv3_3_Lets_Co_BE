package co.kr.user.controller;

import co.kr.user.model.dto.retrieve.*;
import co.kr.user.service.RetrieveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class RetrieveController {
    private final RetrieveService retrieveService;

    @PostMapping("/findID/findMail")
    public ResponseEntity<FindIDFirstStepDTO> findID(@RequestBody @Valid FindIDFirstStepReq findIDFirstStepReq) {
        return ResponseEntity.ok(retrieveService.findIdFirst(findIDFirstStepReq.getMail()));
    }

    @PostMapping("/findID/getID")
    public ResponseEntity<String> findID(@RequestBody @Valid FindIDSecondStepReq findIDSecondStepReq) {
        return ResponseEntity.ok(retrieveService.findIdSecond(findIDSecondStepReq));
    }

    @PostMapping("/findPW/findMail")
    public ResponseEntity<FindPWFirstStepDTO> findPW(@RequestBody @Valid FindPWFirstStepReq findPWFirstStepReq) {
        return ResponseEntity.ok(retrieveService.findPwFirst(findPWFirstStepReq.getMail()));
    }

    @PostMapping("/findPW/setPW")
    public ResponseEntity<String> findPW(@RequestBody @Valid FindPWSecondStepReq findPWSecondStepReq) {
        return ResponseEntity.ok(retrieveService.findPwSecond(findPWSecondStepReq));
    }
}