package co.kr.costomerservice.qnaProduct.model.response;

import java.util.List;

public record QnaProductForSellerListResponse(
        List<QnaProductForSellerResponse> items
) {
}
