package co.kr.user.controller;

import co.kr.user.model.dto.retrieve.*;
import co.kr.user.service.RetrieveService;
import co.kr.user.util.BaseResponse;
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
    public ResponseEntity<BaseResponse<FindIDFirstStepDTO>> findID(@RequestBody @Valid FindIDFirstStepReq findIDFirstStepReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", retrieveService.findIdFirst(findIDFirstStepReq.getMail())));
    }

    @PostMapping("/findID/getID")
    public ResponseEntity<BaseResponse<String>> findID(@RequestBody @Valid FindIDSecondStepReq findIDSecondStepReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", retrieveService.findIdSecond(findIDSecondStepReq)));
    }

    @PostMapping("/findPW/findMail")
    public ResponseEntity<BaseResponse<FindPWFirstStepDTO>> findPW(@RequestBody @Valid FindPWFirstStepReq findPWFirstStepReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", retrieveService.findPwFirst(findPWFirstStepReq.getMail())));
    }

    @PostMapping("/findPW/setPW")
    public ResponseEntity<BaseResponse<String>> findPW(@RequestBody @Valid FindPWSecondStepReq findPWSecondStepReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", retrieveService.findPwSecond(findPWSecondStepReq)));
    }
}