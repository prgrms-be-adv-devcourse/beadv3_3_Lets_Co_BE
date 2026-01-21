package co.kr.costomerservice.notice.model.dto.response;

import co.kr.costomerservice.common.vo.CustomerServiceCategory;

import java.time.LocalDateTime;

public record NoticeDetailResponse(
        String resultCode,

        CustomerServiceCategory category,
        String title,
        String content,
        Long viewCount,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
}
