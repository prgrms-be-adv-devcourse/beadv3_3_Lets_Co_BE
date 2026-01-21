package co.kr.costomerservice.inquiryAdmin.dto.response;

import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;
import co.kr.costomerservice.inquiryAdmin.dto.InquiryDTO;
import co.kr.costomerservice.inquiryAdmin.dto.InquiryDetailDTO;

import java.time.LocalDateTime;
import java.util.List;

public record InquiryDetailResponse(
        String resultCode,

        boolean isOwner,

        InquiryDTO info,

        List<InquiryDetailDTO> details

) {
}
