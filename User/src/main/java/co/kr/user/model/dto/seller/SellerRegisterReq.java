package co.kr.user.model.dto.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 일반 사용자가 판매자(Seller) 권한을 신청할 때 제출하는 정보를 담는 요청 DTO입니다.
 * 사업자 정보 및 정산을 위한 계좌 정보에 대한 유효성 검증을 수행합니다.
 */
@Data
public class SellerRegisterReq {

    /** 판매자(상점) 명칭입니다. 2자 이상 50자 이하로 입력해야 합니다. */
    @NotBlank(message = "판매자 명은 필수 입력 값입니다.")
    @Size(min = 2, max = 50, message = "판매자 명은 2자 이상 50자 이하로 입력해주세요.")
    private String sellerName;

    /** 사업자 등록번호입니다. '000-00-00000' 형식을 준수해야 합니다. */
    @NotBlank(message = "사업자 등록번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자 등록번호 형식이 올바르지 않습니다. (예: 123-45-67890)")
    private String businessLicense;

    /** 정산받을 은행의 이름입니다. */
    @NotBlank(message = "은행명은 필수 입력 값입니다.")
    private String bankBrand;

    /** 계좌의 예금주 성함입니다. */
    @NotBlank(message = "예금주는 필수 입력 값입니다.")
    @Size(max = 20, message = "예금주 성함이 너무 깁니다.")
    private String bankName;

    /** 정산 계좌번호입니다. 보안을 위해 내부적으로 암호화되어 관리됩니다. */
    @NotBlank(message = "계좌번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^[0-9-]+$", message = "계좌번호는 숫자와 하이픈(-)만 포함할 수 있습니다.")
    @Size(min = 10, max = 30, message = "계좌번호 형식이 유효하지 않습니다.")
    private String bankToken;
}