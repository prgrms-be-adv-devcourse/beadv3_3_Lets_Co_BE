package co.kr.customerservice.notice.model.dto.response;

import co.kr.customerservice.common.model.vo.CustomerServiceCategory;
import co.kr.customerservice.common.model.vo.CustomerServiceStatus;

import java.time.LocalDateTime;

public record AdminNoticeDetailRes(
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
