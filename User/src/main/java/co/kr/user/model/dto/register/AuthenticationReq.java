package co.kr.user.model.dto.register;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 회원가입 2단계인 이메일 인증 시 사용자가 입력한 인증 코드를 전달받는 DTO입니다.
 */
@Data
public class AuthenticationReq {
    /**
     * 사용자가 이메일로 받은 본인 확인용 인증 코드입니다.
     */
    @NotBlank(message = "인증코드를 입력해 주세요.")
    @JsonProperty("authCode")
    private String authCode;
}