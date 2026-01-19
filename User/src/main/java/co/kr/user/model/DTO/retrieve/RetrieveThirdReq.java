package co.kr.user.model.DTO.retrieve;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
public class RetrieveThirdReq {

    @JsonProperty("ID")
    private String ID;
    private String authCode;

    // 변경할 새 비밀번호이므로 로그 노출 금지
    @ToString.Exclude // [핵심]
    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 30 characters.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,16}$",
            message = "Password must include at least one letter (lowercase or uppercase), one number, and one special character.")
    @JsonProperty("password")
    private String password;

    // 비밀번호 확인 필드도 로그 노출 금지
    @ToString.Exclude // [핵심]
    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 30 characters.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,16}$",
            message = "Password must include at least one letter (lowercase or uppercase), one number, and one special character.")
    @JsonProperty("passwordCheck")
    private String passwordCheck;
}
