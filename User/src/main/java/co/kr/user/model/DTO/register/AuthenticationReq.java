package co.kr.user.model.DTO.register;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthenticationReq {

    @NotBlank(message = "인증코드를 입력해 주세요.")
    @JsonProperty("code")
    private String code;
}
