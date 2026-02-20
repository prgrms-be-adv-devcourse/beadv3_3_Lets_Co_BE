package co.kr.user.model.dto.retrieve;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 비밀번호 재설정을 위해 가입된 이메일 주소를 확인하고 인증번호 발송을 요청하는 DTO입니다.
 */
@Data
public class FindPWFirstStepReq {
    /** 비밀번호를 재설정할 계정의 이메일 주소입니다. */
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @JsonProperty("Mail")
    private String mail;
}