package co.kr.user.model.DTO.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
public class LoginReq {

    /**
     * [아이디(이메일)]
     * 사용자의 이메일 주소를 아이디로 사용합니다.
     * * @NotBlank: null, 빈 문자열(""), 공백(" ")을 허용하지 않습니다.
     * @Email: 일반적인 이메일 형식(user@domain.com)인지 검사합니다.
     * @JsonProperty("ID"): JSON 데이터의 키 값이 "ID"로 들어오면 이 필드에 매핑합니다. (대문자 매핑)
     */
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @JsonProperty("ID")
    private String ID;

    /**
     * [비밀번호]
     * 보안을 위해 복잡한 비밀번호 규칙을 강제합니다.
     * * @Size: 최소 8자 이상, 최대 16자 이하이어야 합니다.
     * @Pattern: 정규식을 통해 '영문 대소문자 중 1개 이상', '숫자 1개 이상', '특수문자 1개 이상'이 반드시 포함되도록 검사합니다.
     */
    @ToString.Exclude // [핵심] 로그 제외 설정
    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 30 characters.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,16}$",
            message = "Password must include at least one letter (lowercase or uppercase), one number, and one special character.")
    @JsonProperty("PW")
    private String PW;
}
