package co.kr.costomerservice.inquiryAdmin.model.dto.request;

import co.kr.costomerservice.common.model.vo.CustomerServiceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InquiryUpsertRequest(

        @Size(max = 64, message = "유효하지 않은 식별자 코드입니다.")
        String detailCode,

        // CustomerServiceCategory 전부 가능
        @NotNull(message = "문의 카테고리를 선택해주세요.")
        CustomerServiceCategory category,

        @NotBlank(message = "제목을 입력해주세요.")
        @Size(min = 2, max = 200, message = "제목은 2자 이상 200자 이하로 입력해주세요.")
        String title,

        @NotBlank(message = "이름을 입력해주세요.")
        @Size(min = 1, max = 10, message = "내용은 최소 1자 이상, 10자 이하로 작성해주세요.")
        String name,

        @NotBlank(message = "문의 내용을 입력해주세요.")
        @Size(min = 10, max = 5000, message = "내용은 최소 10자 이상, 5000자 이하로 작성해주세요.")
        String content,
        boolean isPrivate

        ) {

}
