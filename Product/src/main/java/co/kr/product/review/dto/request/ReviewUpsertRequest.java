package co.kr.product.review.dto.request;

public class ReviewUpsertRequest {

    private Long orderItemIdx;   // 작성시에 필수, 수정에는 선택으로 둬도 되지만 편의상 포함
    private Integer evaluation;  // 1~5
    private String content;

    public Long getOrderItemIdx() { return orderItemIdx; }
    public Integer getEvaluation() { return evaluation; }
    public String getContent() { return content; }
}


