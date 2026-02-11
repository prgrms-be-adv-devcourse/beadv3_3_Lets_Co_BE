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

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class RegisterController {
    private final RegisterService registerService;

    @PostMapping("/signup/check")
    public ResponseEntity<BaseResponse<String>> duplicateCheck(@RequestBody @Valid SignUpCheckReq signUpCheckReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", registerService.checkDuplicate(signUpCheckReq.getId())));
    }

    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<RegisterDTO>> signup(@RequestBody @Valid RegisterReq registerReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", registerService.signup(registerReq)));
    }

    @PostMapping("/signup/Authentication")
    public ResponseEntity<BaseResponse<String>> signupAuthentication(@RequestBody @Valid AuthenticationReq authenticationReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", registerService.signupAuthentication(authenticationReq.getCode())));
    }
}