package co.kr.order.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/*
 * 요청 상품 정보
 * @param productCode: 제품 코드
 * @param optionCode: 제품 옵션 코드
 * @param quantity: 개수
 */
public record ProductInfo(
        @NotNull(message = "상품 코드는 필수입니다.")
        String productCode,

        @NotNull(message = "옵션 코드는 필수입니다.")
        String optionCode,

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
        Integer quantity
) {}
