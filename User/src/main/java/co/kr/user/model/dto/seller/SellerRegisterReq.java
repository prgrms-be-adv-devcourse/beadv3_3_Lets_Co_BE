package co.kr.user.model.dto.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerRegisterReq {

    @NotBlank(message = "판매자 명은 필수 입력 값입니다.")
    @Size(min = 2, max = 50, message = "판매자 명은 2자 이상 50자 이하로 입력해주세요.")
    private String sellerName;

    @NotBlank(message = "사업자 등록번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자 등록번호 형식이 올바르지 않습니다. (예: 123-45-67890)")
    private String businessLicense;

    @NotBlank(message = "은행명은 필수 입력 값입니다.")
    private String bankBrand;

    @NotBlank(message = "예금주는 필수 입력 값입니다.")
    @Size(max = 20, message = "예금주 성함이 너무 깁니다.")
    private String bankName;

    @NotBlank(message = "계좌번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^[0-9-]+$", message = "계좌번호는 숫자와 하이픈(-)만 포함할 수 있습니다.")
    @Size(min = 10, max = 30, message = "계좌번호 형식이 유효하지 않습니다.")
    private String bankToken;
}