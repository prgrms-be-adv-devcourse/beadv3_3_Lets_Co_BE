package co.kr.costomerservice.qnaProduct.model.request;

import co.kr.costomerservice.common.model.vo.CustomerServiceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record QnaProductUpsertRequest(

        @NotNull(message = "카테고리는 필수입니다.")
        CustomerServiceCategory category,

        @NotBlank(message = "제목을 입력해주세요.")
        @Size(min = 2, max = 200, message = "제목은 최소 2자, 최대 200자를 넘을 수 없습니다.")
        String title,

        boolean isPrivate,

        @Size(max = 64)
        String detailCode, // 수정용

        @Size(max = 50, message = "작성자 이름은 50자를 넘을 수 없습니다.")
        String name,

        @Size(max = 64)
        String parentCode,

        @NotBlank(message = "내용을 입력해주세요.")
        @Size(min = 5, max = 3000, message = "내용은 5자 이상 3000자 이하로 입력해주세요.")
        String content
        ) {
}
