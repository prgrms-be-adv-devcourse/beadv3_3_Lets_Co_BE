package co.kr.user.model.dto.my;

import co.kr.user.model.vo.UsersInformationGender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserAmendReq {
    @Email(message = "유효하지 않은 이메일 형식입니다.")
    private String mail;

    private UsersInformationGender gender;

    private String name;

    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
    private String phoneNumber;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "생년월일 형식(YYYY-MM-DD)이 올바르지 않습니다.")
    private String birth;
}