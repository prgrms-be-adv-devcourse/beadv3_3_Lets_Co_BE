package co.kr.user.model.dto.my;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 회원 탈퇴 절차의 2단계에서 이메일 등으로 발송된
 * 인증 코드를 검증하기 위해 요청받는 DTO입니다.
 */
@Data
public class UserDeleteSecondStepReq {
    /**
     * 사용자가 입력한 본인 확인용 인증 코드입니다.
     * JSON 프로퍼티명은 'authCode'로 매핑됩니다.
     */
    @NotBlank(message = "인증코드를 입력해주세요.")
    @JsonProperty("authCode")
    private String authCode;
}