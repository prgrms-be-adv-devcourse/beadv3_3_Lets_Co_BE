package co.kr.costomerservice.qnaProduct.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QnaAnswerUpsertRequest(

        // 필수로 받아야 하나?
        @Size(max = 50)
        String userName,

        @NotBlank(message = "원글 정보(parentCode)가 누락되었습니다.")
        @Size(max = 64)
        String parentCode,

        @NotBlank(message = "답변 내용을 입력해주세요.")
        @Size(max = 5000)
        String content
) {
}
