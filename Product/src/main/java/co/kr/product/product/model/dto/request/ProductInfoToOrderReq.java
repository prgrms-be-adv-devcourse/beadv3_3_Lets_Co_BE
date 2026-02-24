package co.kr.product.product.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductInfoToOrderReq(

        @NotNull(message = "상품 코드는 필수입니다.")
        String productCode,

        @NotNull(message = "옵션 코드는 필수입니다.")
        String optionCode
) {

}
