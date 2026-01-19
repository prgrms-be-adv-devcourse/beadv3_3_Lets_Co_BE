package co.kr.costomerservice.inquiryAdmin.dto.response;

import co.kr.costomerservice.inquiryAdmin.dto.InquiryDTO;

import java.util.List;

public record InquiryListResponse(
        String resultCode,
        List<InquiryDTO> info
) {

}
