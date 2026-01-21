package co.kr.costomerservice.notice.model.dto.response;

import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;

import java.time.LocalDateTime;

public record AdminNoticeDetailResponse(
        String resultCode,

        String csCode,
        String csDetailCode,


        CustomerServiceCategory category,
        CustomerServiceStatus status,
        String title,
        String content,
        Long viewCount,
        boolean isPinned,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
}
