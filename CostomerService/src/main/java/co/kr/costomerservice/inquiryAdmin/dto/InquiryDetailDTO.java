package co.kr.costomerservice.inquiryAdmin.dto;

import java.time.LocalDateTime;

public record InquiryDetailDTO(

        String detailCode,
        String content,
        LocalDateTime detailCreatedAt

) {
}
