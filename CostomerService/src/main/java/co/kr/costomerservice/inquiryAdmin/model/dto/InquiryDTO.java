package co.kr.costomerservice.inquiryAdmin.model.dto;

import co.kr.costomerservice.common.model.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.model.vo.CustomerServiceStatus;

import java.time.LocalDateTime;

public record InquiryDTO(
        String code,
        // CustomerServiceCategory 전부 가능
        CustomerServiceCategory category,
        // WAITING, ANSWERED, CLOSED, HIDDEN ,PUBLISHED(답변 전용)
        CustomerServiceStatus status,
        String title,
        boolean isPrivate,
        // 문의 등록 시간
        LocalDateTime inquiryCreatedAt
) {
}
