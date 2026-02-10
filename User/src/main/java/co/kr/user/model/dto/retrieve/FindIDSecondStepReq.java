package co.kr.user.model.dto.retrieve;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
public class FindIDSecondStepReq {
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @JsonProperty("Mail")
    private String mail;

    @NotBlank(message = "인증코드를 입력해 주세요.")
    @JsonProperty("authCode")
    private String authCode;
}