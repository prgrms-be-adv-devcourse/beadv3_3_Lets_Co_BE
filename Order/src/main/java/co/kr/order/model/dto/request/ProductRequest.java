package co.kr.order.model.dto.request;

import jakarta.validation.constraints.NotNull;

/*
 * @param productIdx
 * @param optionIdx
 */
public record ProductRequest(
        @NotNull(message = "상품 ID는 필수입니다.")
        Long productIdx,

        @NotNull(message = "옵션 ID는 필수입니다.")
        Long optionIdx
) { }
