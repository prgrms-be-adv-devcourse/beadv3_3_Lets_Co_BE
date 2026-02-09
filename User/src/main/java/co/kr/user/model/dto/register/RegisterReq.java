package co.kr.user.model.dto.register;

import co.kr.user.model.vo.UsersInformationGender;
import co.kr.user.util.validator.ValiDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
public class RegisterReq {
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @JsonProperty("ID")
    private String id;

    @ToString.Exclude // [핵심] 로그에서 비밀번호 제외
    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,16}$",
            message = "Password must include at least one letter (lowercase or uppercase), one number, and one special character.")
    @JsonProperty("PW")
    private String pw;

    @NotNull(message = "이용약관 동의는 필수입니다.")
    private LocalDateTime agreeTermsAt;

    @NotNull(message = "이용약관 동의는 필수입니다.")
    private LocalDateTime agreePrivateAt;

    @NotNull(message = "성별 선택은 필수입니다.")
    private UsersInformationGender gender;

    @ToString.Exclude
    @NotBlank(message = "Name cannot be empty.")
    @Pattern(regexp = "^[가-힣a-zA-Z ]+$", message = "Name must contain only letters and spaces.")
    @Size(max = 50, message = "Name must not exceed 50 characters.")
    private String name;

    @ToString.Exclude
    @NotBlank(message = "휴대폰 번호를 입력해주세요.")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
            message = "휴대폰 번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    private String phoneNumber;

    @ToString.Exclude
    @NotBlank(message = "생년월일을 입력해주세요.")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$",
            message = "생년월일 형식이 올바르지 않습니다. (예: 1990-01-01)")
    @ValiDate
    private String birth;

    private LocalDateTime agreeMarketingAt;
}