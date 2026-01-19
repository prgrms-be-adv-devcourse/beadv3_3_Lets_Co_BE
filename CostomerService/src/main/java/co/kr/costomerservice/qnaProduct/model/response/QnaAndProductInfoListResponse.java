package co.kr.costomerservice.qnaProduct.model.response;

import java.util.List;

public record QnaAndProductInfoListResponse(
        String resultCode,
        List<QnaAndProductInfoResponse> items
) {
}
