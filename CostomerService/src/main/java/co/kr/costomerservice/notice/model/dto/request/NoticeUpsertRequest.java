package co.kr.costomerservice.notice.model.dto.request;

import co.kr.costomerservice.common.model.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.model.vo.CustomerServiceStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record NoticeUpsertRequest(
        @NotNull(message = "카테고리는 필수입니다.")
        CustomerServiceCategory category,

        @NotNull(message = "상태는 필수입니다.")
        CustomerServiceStatus status,

        @NotBlank(message = "제목은 필수입니다.")
        @Size(min = 2, max = 200, message = "제목은 2자 이상 200자 이하로 입력해주세요.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 10000, message = "내용은 10,000자를 초과할 수 없습니다.")
        String content,
        boolean isPrivate,
        boolean isPinned,
        LocalDateTime publishedAt
) {
}
