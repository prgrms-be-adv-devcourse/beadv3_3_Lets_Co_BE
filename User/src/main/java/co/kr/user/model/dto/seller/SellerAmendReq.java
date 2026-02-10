package co.kr.user.model.dto.seller;

import lombok.Data;

@Data
public class SellerAmendReq {
    private String sellerName;
    private String bankBrand;
    private String bankName;
    private String bankToken;
}
