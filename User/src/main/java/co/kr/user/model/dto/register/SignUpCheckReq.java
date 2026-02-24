package co.kr.user.model.dto.register;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 회원가입 과정에서 사용자가 입력한 아이디의 중복 여부를 확인하기 위한 요청 DTO입니다.
 */
@Data
public class SignUpCheckReq {
    /**
     * 중복 확인을 요청할 사용자 아이디입니다.
     * 7자에서 11자 사이의 길이를 가져야 하며, JSON 데이터 매핑 시 "ID" 키를 사용합니다.
     */
    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 7, max = 11, message = "아이디는 7자에서 11자 사이로 입력해주세요.")
    @JsonProperty("ID")
    private String id;
}