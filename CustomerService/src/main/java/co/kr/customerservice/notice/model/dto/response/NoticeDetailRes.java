package co.kr.customerservice.notice.model.dto.response;

import co.kr.customerservice.common.model.vo.CustomerServiceCategory;

import java.time.LocalDateTime;

public record NoticeDetailRes(
        CustomerServiceCategory category,
        String title,
        String content,
        Long viewCount,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
}
