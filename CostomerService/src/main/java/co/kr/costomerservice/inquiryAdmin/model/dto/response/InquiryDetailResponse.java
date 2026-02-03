package co.kr.costomerservice.inquiryAdmin.model.dto.response;

import co.kr.costomerservice.inquiryAdmin.model.dto.InquiryDTO;
import co.kr.costomerservice.inquiryAdmin.model.dto.InquiryDetailDTO;

import java.util.List;

public record InquiryDetailResponse(
        String resultCode,

        boolean isOwner,

        InquiryDTO info,

        List<InquiryDetailDTO> details

) {
}
