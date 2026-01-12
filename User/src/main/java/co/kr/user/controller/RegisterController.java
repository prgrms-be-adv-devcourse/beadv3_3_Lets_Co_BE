package co.kr.user.controller;

import co.kr.user.service.RegisterService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class RegisterController {

    private RegisterService registerService;


    @GetMapping("/duplicate-check")
    public ResponseEntity<String> duplicateCheck(@RequestParam("email")
                                                     @NotBlank(message = "이메일 사용이 불가능합니다")
                                                     @Email(message = "이메일 사용이 불가능합니다")
                                                     @Size(max = 50, message = "이메일 사용이 불가능합니다")
                                                      String email) {
        log.info("=======================================================");
        log.info("duplicate-check - Checking if E-Mail is available");
        log.info("email : {}", email);
        log.info("=======================================================");
        return ResponseEntity.ok(registerService.checkDuplicate(email));
    }

}
