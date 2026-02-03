package co.kr.customerservice.inquiryAdmin.service;

import co.kr.customerservice.common.model.dto.response.ResultResponse;
import co.kr.customerservice.inquiryAdmin.model.dto.request.InquiryUpsertRequest;
import co.kr.customerservice.inquiryAdmin.model.dto.response.InquiryDetailResponse;
import co.kr.customerservice.inquiryAdmin.model.dto.response.InquiryListResponse;
import org.springframework.data.domain.Pageable;

public interface InquiryAdminService {

    InquiryListResponse getInquiryList(Pageable pageable);

    InquiryDetailResponse addInquiry(Long userId, InquiryUpsertRequest request);

    InquiryDetailResponse updateInquiry(Long userId, String inquiryCode,InquiryUpsertRequest request);

    InquiryDetailResponse getInquiryDetail(Long userId, String inquiryCode);

    ResultResponse deleteInquiry(Long userId, String inquiryCode);

    InquiryListResponse getMyInquiryList(Long userId, Pageable pageable);
}
