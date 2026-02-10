package co.kr.user.model.dto.register;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpCheckReq {
    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 7, max = 11, message = "아이디는 7자에서 11자 사이로 입력해주세요.")
    @JsonProperty("ID")
    private String id;
}