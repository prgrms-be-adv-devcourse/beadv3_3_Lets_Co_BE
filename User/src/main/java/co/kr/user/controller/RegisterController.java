package co.kr.user.controller;

import co.kr.user.model.dto.register.AuthenticationReq;
import co.kr.user.model.dto.register.RegisterDTO;
import co.kr.user.model.dto.register.RegisterReq;
import co.kr.user.model.dto.register.SignUpCheckReq;
import co.kr.user.service.RegisterService;
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
    public ResponseEntity<String> duplicateCheck(@RequestBody @Valid SignUpCheckReq signUpCheckReq) {
        return ResponseEntity.ok(registerService.checkDuplicate(signUpCheckReq.getId()));
    }

    @PostMapping("/signup")
    public ResponseEntity<RegisterDTO> signup(@RequestBody @Valid RegisterReq registerReq) {
        return ResponseEntity.ok(registerService.signup(registerReq));
    }

    @PostMapping("/signup/Authentication")
    public ResponseEntity<String> signupAuthentication(@RequestBody @Valid AuthenticationReq authenticationReq) {
        return ResponseEntity.ok(registerService.signupAuthentication(authenticationReq.getCode()));
    }
}