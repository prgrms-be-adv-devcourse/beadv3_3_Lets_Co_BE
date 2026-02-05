package co.kr.customerservice.inquiryAdmin.model.dto;

import java.time.LocalDateTime;

public record InquiryDetailDTO(

        String detailCode,
        String content,
        LocalDateTime detailCreatedAt

) {
}
