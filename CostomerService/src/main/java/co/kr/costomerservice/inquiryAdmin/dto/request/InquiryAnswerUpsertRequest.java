package co.kr.costomerservice.inquiryAdmin.dto.request;

public record InquiryAnswerUpsertRequest(
        String detailCode,
        String content
) {
}
