package co.kr.product.review.model.dto.response;

import java.util.List;

public class ReviewListResponse {

    private String resultCode;
    private List<ReviewResponse> items;

    public ReviewListResponse(String resultCode, List<ReviewResponse> items) {
        this.resultCode = resultCode;
        this.items = items;
    }

    public String getResultCode() { return resultCode; }
    public List<ReviewResponse> getItems() { return items; }
}

