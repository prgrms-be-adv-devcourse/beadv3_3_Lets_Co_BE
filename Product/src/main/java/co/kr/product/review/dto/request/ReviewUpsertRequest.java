package co.kr.product.review.dto.request;

import jakarta.validation.constraints.*;

public class ReviewUpsertRequest {

    @Positive(message = "유효하지 않은 주문 아이템 ID입니다.")
    private Long orderItemIdx;   // 작성시에 필수, 수정에는 선택으로 둬도 되지만 편의상 포함

    @NotNull(message = "별점은 필수입니다.")
    @Min(value = 1, message = "별점은 최소 1점 이상이어야 합니다.")
    @Max(value = 5, message = "별점은 최대 5점까지 가능합니다.")
    private Integer evaluation;  // 1~5

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(max = 5000, message = "리뷰 내용은 5000자 이하여야 합니다.")
    private String content;

    public Long getOrderItemIdx() { return orderItemIdx; }
    public Integer getEvaluation() { return evaluation; }
    public String getContent() { return content; }
}


