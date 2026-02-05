package co.kr.order.model.dto.request;

import jakarta.validation.constraints.NotNull;

public record CartReq(
        @NotNull(message = "상품코드는 필수입니다.")
        String productCode,

        @NotNull(message = "옵션코드는 필수입니다.")
        String optionCode
) { }
