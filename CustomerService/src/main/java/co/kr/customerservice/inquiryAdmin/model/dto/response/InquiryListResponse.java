package co.kr.customerservice.inquiryAdmin.model.dto.response;

import co.kr.customerservice.inquiryAdmin.model.dto.InquiryDTO;

import java.util.List;

public record InquiryListResponse(
        String resultCode,
        List<InquiryDTO> list
) {

}
