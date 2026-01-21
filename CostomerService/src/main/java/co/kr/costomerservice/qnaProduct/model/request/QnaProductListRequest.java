package co.kr.costomerservice.qnaProduct.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record QnaProductListRequest(
        @NotNull(message = "상품 Idx는 필수입니다.")
        @Positive(message = "유효하지 않은 상품 Idx 입니다.")
        Long productsIdx
) {
}
