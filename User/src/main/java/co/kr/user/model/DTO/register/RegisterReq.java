package co.kr.user.model.DTO.register;

import co.kr.user.util.validator.ValiDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * [회원가입 요청 DTO]
 * 클라이언트(프론트엔드)로부터 전달받는 회원가입 정보를 담는 객체입니다.
 * 각 필드에 어노테이션을 사용하여 입력값의 유효성(Validation)을 엄격하게 검사합니다.
 */
@Data // Lombok: Getter, Setter, toString, equals, hashCode 등을 자동으로 생성합니다.
public class RegisterReq {

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
    @ToString.Exclude // [핵심] 로그에서 비밀번호 제외
    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,16}$",
            message = "Password must include at least one letter (lowercase or uppercase), one number, and one special character.")
    @JsonProperty("PW")
    private String PW;

    /**
     * [이용약관 동의 일시]
     * 필수 동의 항목입니다. 동의한 시간이 기록되어야 하며 null일 수 없습니다.
     */
    @NotNull(message = "이용약관 동의는 필수입니다.")
    private LocalDateTime agreeTermsAt;

    /**
     * [개인정보 처리방침 동의 일시]
     * 필수 동의 항목입니다.
     */
    @NotNull(message = "이용약관 동의는 필수입니다.")
    private LocalDateTime agreePrivateAt;

    /**
     * [마케팅 정보 수신 동의 일시]
     * 선택 동의 항목이므로 @NotNull 검증이 없으며, 값이 없으면 null로 처리됩니다.
     */
    private LocalDateTime agreeMarketingAt;

    /**
     * [이름]
     * 사용자의 실명입니다.
     * * @Pattern: 한글, 영문 대소문자, 공백만 허용합니다. (특수문자, 숫자 불가)
     * @Size: DB 컬럼 길이에 맞춰 최대 50자까지만 허용합니다.
     */
    @ToString.Exclude // [권장] 개인정보 제외 (필요시 암호화된 값만 찍히게 하거나 아예 제외)
    @NotBlank(message = "Name cannot be empty.")
    @Pattern(regexp = "^[가-힣a-zA-Z ]+$", message = "Name must contain only letters and spaces.")
    @Size(max = 50, message = "Name must not exceed 50 characters.")
    private String name;

    /**
     * [휴대폰 번호]
     * 연락처 정보입니다.
     * * @Pattern: '010-1234-5678'과 같은 형식을 정확히 지켜야 합니다.
     * (010, 011 등으로 시작하고 중간 3~4자리, 끝 4자리 숫자)
     */
    @ToString.Exclude // [권장] 전화번호 제외
    @NotBlank(message = "휴대폰 번호를 입력해주세요.")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
            message = "휴대폰 번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    private String phoneNumber;

    /**
     * [생년월일]
     * 사용자의 생년월일 문자열입니다. (예: "1990-01-01")
     * * @Pattern: 'YYYY-MM-DD' 형식을 검사합니다.
     * (월은 01~12, 일은 01~31 범위 내 숫자여야 함)
     * @ValiDate: 개발자가 직접 만든 커스텀 어노테이션입니다.
     * 단순 형식뿐만 아니라 '미래 날짜 불가', '존재하지 않는 날짜(2월 30일 등) 불가' 등 논리적 검증을 수행합니다.
     */
    @ToString.Exclude // [권장] 생년월일 제외
    @NotBlank(message = "생년월일을 입력해주세요.")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$",
            message = "생년월일 형식이 올바르지 않습니다. (예: 1990-01-01)")
    @ValiDate
    private String birth;
}