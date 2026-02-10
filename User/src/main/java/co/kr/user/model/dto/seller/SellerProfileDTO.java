package co.kr.user.model.dto.seller;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SellerProfileDTO {
    private String sellerName;
    private String businessLicense;
    private String bankBrand;
    private String bankName;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

}