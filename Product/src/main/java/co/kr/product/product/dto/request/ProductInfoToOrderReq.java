package co.kr.product.product.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductInfoToOrderReq(

        @NotNull(message = "상품 ID는 필수입니다.")
        @Positive(message = "유효하지 않은 상품 ID입니다.")
        Long productIdx,

        @NotNull(message = "옵션 ID는 필수입니다.")
        @Positive(message = "유효하지 않은 옵션 ID입니다.")
        Long optionIdx
) {

}
