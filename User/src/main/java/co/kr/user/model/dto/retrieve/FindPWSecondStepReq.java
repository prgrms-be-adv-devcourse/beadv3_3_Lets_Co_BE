package co.kr.user.model.dto.retrieve;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * 인증번호 확인 후 사용자의 비밀번호를 새로운 값으로 재설정하기 위한 요청 DTO입니다.
 * 강력한 비밀번호 정책(영문, 숫자, 특수문자 조합) 검증 로직이 포함되어 있습니다.
 */
@Data
public class FindPWSecondStepReq {
    /** 본인 확인용 이메일 주소입니다. */
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @JsonProperty("Mail")
    private String mail;

    /** 이메일로 수신한 인증 코드입니다. */
    @NotBlank(message = "인증코드를 입력해 주세요.")
    @JsonProperty("authCode")
    private String authCode;

    /** 새롭게 설정할 비밀번호입니다. */
    @ToString.Exclude
    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,16}$",
            message = "Password must include at least one letter (lowercase or uppercase), one number, and one special character.")
    @JsonProperty("newPW")
    private String newPW;

    /** 비밀번호 확인을 위해 한 번 더 입력한 값입니다. */
    @ToString.Exclude
    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,16}$",
            message = "Password must include at least one letter (lowercase or uppercase), one number, and one special character.")
    @JsonProperty("newPWCheck")
    private String newPWCheck;
}