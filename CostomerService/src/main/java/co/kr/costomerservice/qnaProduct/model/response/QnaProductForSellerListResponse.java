package co.kr.costomerservice.qnaProduct.model.response;

import java.util.List;

public record QnaProductForSellerListResponse(
        String resultCode,
        List<QnaProductForSellerResponse> items
) {
}
