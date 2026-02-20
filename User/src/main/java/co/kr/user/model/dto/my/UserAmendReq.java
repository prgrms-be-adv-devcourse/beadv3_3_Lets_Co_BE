package co.kr.user.model.dto.my;

import co.kr.user.model.vo.UsersInformationGender;
import co.kr.user.util.validator.ValiDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 마이페이지에서 회원 정보를 수정하기 위해 사용자가 입력한 데이터를 서버로 전달하는 DTO입니다.
 * 이메일, 연락처, 생년월일 등 변경 가능한 필드들에 대한 유효성 검증 로직을 포함합니다.
 */
@Data
public class UserAmendReq {
    /** 수정을 요청한 사용자의 로그인 아이디 */
    private String id;

    /** 변경할 이메일 주소 (이메일 형식 검증) */
    @Email(message = "이메일 형식이 아닙니다.")
    private String mail;

    /** 변경할 성별 (MALE, FEMALE, OTHER) */
    @JsonProperty("gender")
    private UsersInformationGender gender;

    /** 변경할 이름 */
    private String name;

    /** 변경할 휴대폰 번호 (010-0000-0000 형식 검증) */
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phoneNumber;

    /** 변경할 생년월일 (YYYYMMDD 형식 및 실제 날짜 여부 검증) */
    @ValiDate(message = "생년월일 형식이 올바르지 않습니다.")
    private String birth;

    /** 마케팅 수신 동의 여부 업데이트를 위한 일시 정보 */
    private LocalDateTime agreeMarketingAt;
}