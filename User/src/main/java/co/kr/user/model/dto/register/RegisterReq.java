package co.kr.user.model.dto.register;

import co.kr.user.model.vo.UsersInformationGender;
import co.kr.user.util.validator.PasswordMatch;
import co.kr.user.util.validator.ValiDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 회원가입 시 클라이언트로부터 전달받는 전체 사용자 정보 DTO입니다.
 * 필드별 유효성 검증(Validation) 및 클래스 레벨의 비밀번호 일치 검증(@PasswordMatch)이 적용되어 있습니다.
 */
@Data
@PasswordMatch // 비밀번호와 비밀번호 확인 필드 일치 여부를 검증하는 커스텀 어노테이션
public class RegisterReq {
    /** 사용자 로그인 아이디 (7~11자, 영문 소문자 및 숫자 형식 제한) */
    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 7, max = 11, message = "아이디는 7자에서 11자 사이로 입력해주세요.")
    @Pattern(regexp = "^[a-z0-9]{7,11}$", message = "아이디는 7~11자의 영문 소문자와 숫자만 사용 가능합니다.")
    @JsonProperty("ID")
    private String id;

    /** 사용자 이메일 (형식 검증 및 최대 100자 제한) */
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 100, message = "이메일은 최대 100자까지 입력 가능합니다.")
    @JsonProperty("Mail")
    private String mail;

    /** 로그인 비밀번호 (영문, 숫자, 특수문자 조합 8~16자) */
    @ToString.Exclude // 로그 출력 시 보안을 위해 제외
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 16, message = "비밀번호는 8자에서 16자 사이로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,16}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 각각 최소 1자 이상 포함해야 합니다.")
    @JsonProperty("PW")
    private String pw;

    /** 비밀번호 확인 입력값 */
    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    @JsonProperty("PW_CHECK")
    private String pwCheck;

    /** 서비스 이용약관 동의 일시 */
    @NotNull(message = "이용약관 동의 일시가 누락되었습니다.")
    private LocalDateTime agreeTermsAt;

    /** 개인정보 수집 및 이용 동의 일시 */
    @NotNull(message = "개인정보 수집 및 이용 동의 일시가 누락되었습니다.")
    private LocalDateTime agreePrivateAt;

    /** 사용자 성별 (MALE, FEMALE, OTHER) */
    @NotNull(message = "성별을 선택해주세요.")
    private UsersInformationGender gender;

    /** 사용자 실명 (한글/영문만 허용) */
    @ToString.Exclude
    @NotBlank(message = "이름을 입력해주세요.")
    @Pattern(regexp = "^[가-힣a-zA-Z ]+$", message = "이름은 한글 또는 영문만 입력 가능합니다.")
    @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
    private String name;

    /** 휴대폰 번호 (010-0000-0000 형식) */
    @ToString.Exclude
    @NotBlank(message = "휴대폰 번호를 입력해주세요.")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
            message = "휴대폰 번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    private String phoneNumber;

    /** 생년월일 (YYYY-MM-DD 형식 및 만 14세 이상 여부 검증) */
    @ToString.Exclude
    @NotBlank(message = "생년월일을 입력해주세요.")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$",
            message = "생년월일 형식이 올바르지 않습니다. (예: 1990-01-01)")
    @ValiDate // 날짜 논리 유효성을 검증하는 커스텀 어노테이션
    private String birth;

    /** 선택사항: 마케팅 수신 동의 일시 */
    private LocalDateTime agreeMarketingAt;
}