package co.kr.order.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderReq(
        @NotNull(message = "상품 ID는 필수입니다.")
        String productCode,

        @NotNull(message = "옵션 ID는 필수입니다.")
        String optionCode,

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
        Integer quantity
) {}
