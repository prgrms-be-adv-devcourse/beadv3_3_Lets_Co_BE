package co.kr.costomerservice.model.dto.response;

import co.kr.costomerservice.vo.CustomerServiceCategory;

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
