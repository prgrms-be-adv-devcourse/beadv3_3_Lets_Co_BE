package co.kr.user.model.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 판매자 자격 삭제(탈퇴) 2단계에서 인증 코드를 검증하기 위해 사용하는 DTO입니다.
 */
@Data
public class SellerDeleteSecondStepReq {
    /**
     * 사용자가 입력한 본인 확인용 이메일 인증 코드입니다.
     */
    @NotBlank(message = "인증코드를 입력해주세요.")
    @JsonProperty("authCode")
    private String authCode;
}