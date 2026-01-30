package co.kr.product.product.dto.request;

import jakarta.validation.constraints.*;

public record ProductImagesReq(

        // 추가 시 idx 안받음
        @Positive(message = "이미지 ID는 양수여야 합니다.")
        Long imageIdx,

        @NotBlank(message = "이미지 URL은 필수입니다.")
        @Size(max = 255, message = "이미지 URL은 255자를 넘을 수 없습니다.")
        String url,

        @NotNull(message = "정렬 순서는 필수입니다.")
        @Min(value = 0, message = "정렬 순서는 0 이상이어야 합니다.")
        Integer sortOrder,

        @NotNull(message = "썸네일 여부는 필수입니다.")
        Boolean isThumbnail
) {
}
