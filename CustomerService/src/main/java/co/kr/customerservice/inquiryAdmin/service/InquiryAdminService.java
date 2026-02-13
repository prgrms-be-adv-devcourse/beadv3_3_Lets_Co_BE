package co.kr.customerservice.inquiryAdmin.service;

import co.kr.customerservice.common.model.dto.response.ResultResponse;
import co.kr.customerservice.inquiryAdmin.model.dto.request.InquiryUpsertReq;
import co.kr.customerservice.inquiryAdmin.model.dto.response.InquiryDetailRes;
import co.kr.customerservice.inquiryAdmin.model.dto.response.InquiryListRes;
import org.springframework.data.domain.Pageable;

public interface InquiryAdminService {

    InquiryListRes getInquiryList(Pageable pageable);

    InquiryDetailRes addInquiry(Long userId, InquiryUpsertReq request);

    InquiryDetailRes updateInquiry(Long userId, String inquiryCode, InquiryUpsertReq request);

    InquiryDetailRes getInquiryDetail(Long userId, String inquiryCode);

    ResultResponse deleteInquiry(Long userId, String inquiryCode);

    InquiryListRes getMyInquiryList(Long userId, Pageable pageable);
}
