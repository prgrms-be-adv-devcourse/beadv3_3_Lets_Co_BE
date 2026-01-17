package co.kr.user.model.DTO.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerRegisterReq {

    // null, "", " " 모두 허용하지 않음
    @NotBlank(message = "인증코드를 입력해 주세요.")
    @JsonProperty("code")
    private String Code; // 참고: Java 변수명 컨벤션상 code(소문자 시작)가 권장됩니다.

    @NotBlank(message = "사업자 등록번호는 필수 입력 값입니다.")
    // 정규식 예시: 000-00-00000 형식 (하이픈 포함 12자리)
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자 등록번호 형식이 올바르지 않습니다. (예: 123-45-67890)")
    private String businessLicense;

    @NotBlank(message = "은행명은 필수 입력 값입니다.")
    private String bankBrand;

    @NotBlank(message = "예금주(또는 은행 지점명)는 필수 입력 값입니다.")
    private String bankName;

    @NotBlank(message = "계좌번호(또는 토큰)는 필수 입력 값입니다.")
    // 계좌번호는 숫자와 하이픈만 허용하고 싶다면 아래 주석 해제하여 사용
    // @Pattern(regexp = "^[0-9-]*$", message = "계좌 정보에는 숫자와 하이픈(-)만 사용할 수 있습니다.")
    private String bankToken;
}