package co.kr.product.product.model.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductOptionsRequest(

        // 추가 시 code를 안 받음
        @Size(max = 50, message = "옵션 코드는 50자를 넘을 수 없습니다.")
        String code,

        @NotBlank(message = "옵션명은 필수입니다.")
        @Size(max = 50, message = "옵션명은 50자를 넘을 수 없습니다.")
        String name,

        @Min(value = 0, message = "정렬 순서는 0 이상이어야 합니다.")
        int sortOrder,

        @NotNull(message = "옵션 가격은 필수입니다.")
        @PositiveOrZero(message = "옵션 가격은 0원 이상이어야 합니다.")
        @Digits(integer = 17, fraction = 2)
        BigDecimal price,

        @PositiveOrZero(message = "옵션 할인가는 0원 이상이어야 합니다.")
        @Digits(integer = 17, fraction = 2)
        BigDecimal salePrice,

        @Min(value = 0, message = "옵션 재고는 0개 이상이어야 합니다.")
        int stock,

        @NotBlank(message = "옵션 상태는 필수입니다.")
        @Size(max = 20, message = "상태값은 20자를 넘을 수 없습니다.")
        String status
        ) {
}
