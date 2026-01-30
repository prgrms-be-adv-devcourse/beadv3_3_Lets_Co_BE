package co.kr.product.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DeductStockReq(

        @NotNull(message = "상품 ID는 필수입니다.")
        @Positive(message = "유효하지 않은 상품 ID입니다.")
        Long productIdx,

        @NotNull(message = "옵션 ID는 필수입니다.")
        @Positive(message = "유효하지 않은 옵션 ID입니다.")
        Long optionIdx,

        // 사용자가 상품을 구매 했을 시 에만 해당 요청이 오므로
        // 차감은 반드시 있어야 함.
        @NotNull(message = "차감할 수량은 필수입니다.")
        @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
        Integer quantity
) {
}
