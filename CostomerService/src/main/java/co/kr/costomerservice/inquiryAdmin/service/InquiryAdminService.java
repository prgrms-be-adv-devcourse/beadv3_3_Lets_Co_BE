package co.kr.costomerservice.inquiryAdmin.service;


import co.kr.costomerservice.common.response.ResultResponse;
import co.kr.costomerservice.inquiryAdmin.dto.request.InquiryUpsertRequest;
import co.kr.costomerservice.inquiryAdmin.dto.response.InquiryDetailResponse;
import co.kr.costomerservice.inquiryAdmin.dto.response.InquiryListResponse;
import org.springframework.data.domain.Pageable;

public interface InquiryAdminService {

    InquiryListResponse getInquiryList(Pageable pageable);

    InquiryDetailResponse addInquiry(Long userId, InquiryUpsertRequest request);

    InquiryDetailResponse updateInquiry(Long userId, String inquiryCode,InquiryUpsertRequest request);

    InquiryDetailResponse getInquiryDetail(Long userId, String inquiryCode);

    ResultResponse deleteInquiry(Long userId, String inquiryCode);

    InquiryListResponse getMyInquiryList(Long userId, Pageable pageable);
}
