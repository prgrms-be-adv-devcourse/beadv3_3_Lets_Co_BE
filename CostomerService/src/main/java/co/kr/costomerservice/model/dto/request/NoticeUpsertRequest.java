package co.kr.costomerservice.model.dto.request;

import co.kr.costomerservice.vo.CustomerServiceCategory;
import co.kr.costomerservice.vo.CustomerServiceStatus;
import org.springframework.cglib.core.Local;

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
