package co.kr.product.review.model.dto.response;

import java.time.LocalDateTime;

public class ReviewResponse {

    private Long reviewIdx;
    private Long productsIdx;
    private Long userIdx;
    private Integer evaluation;
    private String content;
    private LocalDateTime createdAt;

    public ReviewResponse(Long reviewIdx, Long productsIdx, Long userIdx,
                          Integer evaluation, String content, LocalDateTime createdAt) {
        this.reviewIdx = reviewIdx;
        this.productsIdx = productsIdx;
        this.userIdx = userIdx;
        this.evaluation = evaluation;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getReviewIdx() { return reviewIdx; }
    public Long getProductsIdx() { return productsIdx; }
    public Long getUserIdx() { return userIdx; }
    public Integer getEvaluation() { return evaluation; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}


