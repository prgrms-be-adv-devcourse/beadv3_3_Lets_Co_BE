package co.kr.costomerservice.qnaProduct.model.response;

import java.util.List;

public record QnaProductListResponse(
        List<QnaProductResponse> items
) {
}
