package co.kr.product.product.dto.request;

import co.kr.product.product.dto.vo.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record UpsertProductRequest(

        // 추가 시 idx를 안 받음
        @Positive(message = "상품 ID는 양수여야 합니다.")
        Long productsIdx,

        @NotBlank(message = "상품명은 필수입니다.")
        @Size(max = 200, message = "상품명은 200자를 초과할 수 없습니다.")
        String name,

        @Size(max = 5000, message = "상품 설명은 5000자를 초과할 수 없습니다.")
        String description,

        @NotNull(message = "가격은 필수입니다.")
        @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
        @Digits(integer = 17, fraction = 2, message = "가격은 정수 17자리, 소수 2자리까지 허용됩니다.")
        BigDecimal price,

        @PositiveOrZero(message = "할인가는 0원 이상이어야 합니다.")
        @Digits(integer = 17, fraction = 2, message = "할인가는 정수 17자리, 소수 2자리까지 허용됩니다.")
        BigDecimal salePrice,

        @NotNull(message = "재고 수량은 필수입니다.")
        @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
        int stock,

        @NotNull(message = "상품 상태값은 필수입니다.")
        ProductStatus status,

        // Valid 붙여야 내부 값 까지 확인
        @Valid
        @NotNull(message = "옵션 리스트는 null일 수 없습니다. (빈 리스트는 허용)")
        List<ProductOptionsRequest> options,

        @Valid
        @NotNull(message = "이미지 리스트는 null일 수 없습니다. (빈 리스트는 허용)")
        List<ProductImagesRequest> images
        ) {
}
