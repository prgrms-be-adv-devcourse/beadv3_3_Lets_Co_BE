package co.kr.customerservice.inquiryAdmin.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record InquiryAnswerDeleteRequest(

        @NotBlank(message = "삭제할 답변의 코드는 필수입니다.")
        String detailCode
) {
}
