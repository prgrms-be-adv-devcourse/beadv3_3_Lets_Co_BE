package co.kr.user.model.dto.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
public class LoginReq {
    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 7, max = 11, message = "아이디는 7자에서 11자 사이로 입력해주세요.")
    @Pattern(regexp = "^[a-z0-9]{7,11}$", message = "아이디는 7~11자의 영문 소문자와 숫자만 사용 가능합니다.")
    @JsonProperty("ID")
    private String id;

    @ToString.Exclude
    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,16}$",
            message = "Password must include at least one letter (lowercase or uppercase), one number, and one special character.")
    @JsonProperty("PW")
    private String pw;
}