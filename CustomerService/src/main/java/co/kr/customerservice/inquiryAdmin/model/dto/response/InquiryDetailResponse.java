package co.kr.customerservice.inquiryAdmin.model.dto.response;

import co.kr.customerservice.inquiryAdmin.model.dto.InquiryDTO;
import co.kr.customerservice.inquiryAdmin.model.dto.InquiryDetailDTO;

import java.util.List;

public record InquiryDetailResponse(
        String resultCode,

        boolean isOwner,

        InquiryDTO info,

        List<InquiryDetailDTO> details

) {
}
