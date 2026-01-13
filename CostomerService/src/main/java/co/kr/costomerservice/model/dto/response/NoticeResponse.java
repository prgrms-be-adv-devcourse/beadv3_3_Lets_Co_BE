package co.kr.costomerservice.model.dto.response;



import co.kr.costomerservice.vo.CustomerServiceCategory;
import co.kr.costomerservice.vo.CustomerServiceStatus;

import java.time.LocalDateTime;

public record NoticeResponse(
        Long Idx,
        String Code,
        CustomerServiceCategory Category,
        String title,
        CustomerServiceStatus status,
        LocalDateTime publishedAt,
        Long viewCount,
        boolean isPrivate,
        boolean isPined,
        LocalDateTime updatedAt
) {
}
