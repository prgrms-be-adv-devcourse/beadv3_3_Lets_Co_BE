package co.kr.user.model.dto.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 판매자 입점 신청 후, 이메일로 받은 인증 코드를 제출할 때 사용하는 DTO입니다.
 */
@Data
public class SellerAuthenticationReq {
    /**
     * 사용자가 입력한 판매자 본인 확인용 인증 코드입니다.
     */
    @NotBlank(message = "인증코드를 입력해주세요.")
    @JsonProperty("authCode")
    private String authCode;
}