package co.kr.user.model.dto.seller;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 판매자 마이페이지 등에서 상점 및 정산 정보를 표시하기 위해 사용하는 DTO입니다.
 */
@Data
public class SellerProfileDTO {
    /** 등록된 상점 또는 판매자명입니다. */
    private String sellerName;
    /** 등록된 사업자 등록번호입니다. */
    private String businessLicense;
    /** 정산 은행명입니다. */
    private String bankBrand;
    /** 예금주 성함입니다. */
    private String bankName;
    /** 판매자 최초 등록 일시입니다. */
    private LocalDateTime createAt;
    /** 판매자 정보 최종 수정 일시입니다. */
    private LocalDateTime updateAt;
}