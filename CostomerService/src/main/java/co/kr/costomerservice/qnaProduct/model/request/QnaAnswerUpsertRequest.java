package co.kr.costomerservice.qnaProduct.model.request;

public record QnaAnswerUpsertRequest(
        String userName,
        String parentCode,
        String content
) {
}
