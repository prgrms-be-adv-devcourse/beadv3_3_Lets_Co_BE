package co.kr.costomerservice.inquiryAdmin.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InquiryAnswerUpsertRequest(
        // 추가 할 때에는 안받음
        @Size(max = 64)
        String detailCode,

        @NotBlank(message = "답변 내용은 필수입니다.")
        @Size(max = 5000, message = "답변은 5000자를 넘을 수 없습니다.")
        String content
) {
}
