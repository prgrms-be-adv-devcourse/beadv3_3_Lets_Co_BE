package co.kr.costomerservice.qnaProduct.model.response;

import java.util.List;

public record QnaProductListResponse(
        String resultCode,
        List<QnaProductResponse> items
) {
}
