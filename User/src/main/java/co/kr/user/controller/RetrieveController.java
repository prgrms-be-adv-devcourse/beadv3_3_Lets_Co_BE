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

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class RetrieveController {
    private final RetrieveService retrieveService;

    @PostMapping("/findPW/findID")
    public ResponseEntity<FindPWFirstStepDTO> findPW(@RequestBody @Valid FindPWFirstStepReq findPWFirstStepReq) {
        return ResponseEntity.ok(retrieveService.findPwFirst(findPWFirstStepReq));
    }

    @PostMapping("/findPW/setPW")
    public ResponseEntity<String> findPW(@RequestBody @Valid FindPWSecondStepReq findPWSecondStepReq) {
        return ResponseEntity.ok(retrieveService.findPwSecond(findPWSecondStepReq));
    }
}