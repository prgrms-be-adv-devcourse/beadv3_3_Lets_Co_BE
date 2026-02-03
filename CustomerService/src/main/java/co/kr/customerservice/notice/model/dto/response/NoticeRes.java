package co.kr.customerservice.notice.model.dto.response;



import co.kr.customerservice.common.model.vo.CustomerServiceCategory;
import co.kr.customerservice.common.model.vo.CustomerServiceStatus;

import java.time.LocalDateTime;

public record NoticeRes(
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
