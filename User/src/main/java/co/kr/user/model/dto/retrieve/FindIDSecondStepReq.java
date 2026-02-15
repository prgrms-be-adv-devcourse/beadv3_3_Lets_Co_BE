package co.kr.user.model.dto.retrieve;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * 아이디 찾기 2단계(인증번호 검증 및 아이디 제공)를 수행하기 위한 요청 DTO입니다.
 */
@Data
public class FindIDSecondStepReq {
    /** 본인 확인을 위한 이메일 주소입니다. */
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @JsonProperty("Mail")
    private String mail;

    /** 이메일로 수신한 인증 코드입니다. */
    @NotBlank(message = "인증코드를 입력해 주세요.")
    @JsonProperty("authCode")
    private String authCode;
}