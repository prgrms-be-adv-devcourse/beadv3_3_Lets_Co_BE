package co.kr.user.model.dto.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SellerRegisterReq {
    @NotBlank(message = "사업자 등록번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자 등록번호 형식이 올바르지 않습니다. (예: 123-45-67890)")
    private String businessLicense;

    @NotBlank(message = "은행명은 필수 입력 값입니다.")
    private String bankBrand;

    @NotBlank(message = "예금주(또는 은행 지점명)는 필수 입력 값입니다.")
    private String bankName;

    @NotBlank(message = "계좌번호(또는 토큰)는 필수 입력 값입니다.")
    private String bankToken;
}