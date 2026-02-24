package co.kr.user.model.dto.retrieve;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 아이디 찾기를 위해 가입된 이메일 주소를 서버로 전달하는 DTO입니다.
 */
@Data
public class FindIDFirstStepReq {
    /**
     * 아이디를 찾고자 하는 사용자의 가입 이메일 주소입니다.
     */
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @JsonProperty("Mail")
    private String mail;
}