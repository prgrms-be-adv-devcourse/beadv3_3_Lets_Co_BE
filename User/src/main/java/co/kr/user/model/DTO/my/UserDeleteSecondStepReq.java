package co.kr.user.model.DTO.my;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class UserDeleteSecondStepReq {

    @NotBlank(message = "인증코드를 입력해주세요.")
    @JsonProperty("authCode")
    private String authCode;
}
