package co.kr.product.review.model.dto.request;

import jakarta.validation.constraints.*;

public record ReviewUpsertRequest(

        @NotNull(message = "별점은 필수입니다.")
        @Min(value = 1, message = "별점은 최소 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 최대 5점까지 가능합니다.")
         Integer evaluation,  // 1~5

        @NotBlank(message = "리뷰 내용은 필수입니다.")
        @Size(max = 5000, message = "리뷰 내용은 5000자 이하여야 합니다.")
         String content
) {

}


