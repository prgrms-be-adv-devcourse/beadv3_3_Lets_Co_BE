package co.kr.user.model.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SellerDeleteSecondStepReq {
    @NotBlank(message = "인증코드를 입력해주세요.")
    @JsonProperty("authCode")
    private String authCode;
}