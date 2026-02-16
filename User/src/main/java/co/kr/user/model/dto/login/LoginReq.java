package co.kr.user.model.dto.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * 사용자가 로그인을 위해 아이디와 비밀번호를 입력했을 때,
 * 해당 정보를 서버로 전달하기 위한 요청 DTO입니다.
 */
@Data
public class LoginReq {
    /**
     * 사용자의 로그인 아이디입니다.
     * 공백일 수 없으며, 유효성 검증을 위해 @NotBlank 어노테이션이 적용되어 있습니다.
     */
    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 7, max = 11, message = "아이디는 7자에서 11자 사이로 입력해주세요.")
    @Pattern(regexp = "^[a-z0-9]{7,11}$", message = "아이디는 7~11자의 영문 소문자와 숫자만 사용 가능합니다.")
    @JsonProperty("ID")
    private String id;

    /**
     * 사용자의 로그인 비밀번호입니다.
     * 보안을 위해 평문 상태로 전달되며, 서버에서 암호화된 값과 비교 검증됩니다.
     */
    @ToString.Exclude
    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,16}$",
            message = "Password must include at least one letter (lowercase or uppercase), one number, and one special character.")
    @JsonProperty("PW")
    private String pw;
}