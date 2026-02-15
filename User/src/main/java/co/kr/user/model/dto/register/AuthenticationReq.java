package co.kr.user.model.dto.register;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthenticationReq {
    @NotBlank(message = "인증코드를 입력해 주세요.")
    @JsonProperty("authCode")
    private String authCode;
}
