package co.kr.costomerservice.notice.model.dto.request;

import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;

import java.time.LocalDateTime;

public record NoticeUpsertRequest(
        CustomerServiceCategory category,
        CustomerServiceStatus status,
        String title,
        String content,
        boolean isPrivate,
        boolean isPinned,
        LocalDateTime publishedAt
) {
}
