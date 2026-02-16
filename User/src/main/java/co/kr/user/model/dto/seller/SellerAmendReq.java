package co.kr.user.model.dto.seller;

import lombok.Data;

/**
 * 판매자 정보를 업데이트하기 위한 데이터를 담는 DTO입니다.
 * 수정이 필요한 필드(상점명, 계좌 등)를 선택적으로 전달받습니다.
 */
@Data
public class SellerAmendReq {
    /** 변경할 판매자(상점) 명칭입니다. */
    private String sellerName;
    /** 변경할 정산 은행명입니다. */
    private String bankBrand;
    /** 변경할 예금주 성함입니다. */
    private String bankName;
    /** 변경할 계좌번호입니다. 입력 시 암호화되어 저장됩니다. */
    private String bankToken;
}