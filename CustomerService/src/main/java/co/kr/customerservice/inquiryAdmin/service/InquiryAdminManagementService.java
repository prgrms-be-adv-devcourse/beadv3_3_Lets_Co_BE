package co.kr.customerservice.inquiryAdmin.service;

import co.kr.customerservice.common.model.dto.response.ResultResponse;
import co.kr.customerservice.inquiryAdmin.model.dto.request.InquiryAnswerDeleteReq;
import co.kr.customerservice.inquiryAdmin.model.dto.request.InquiryAnswerUpsertReq;
import co.kr.customerservice.inquiryAdmin.model.dto.request.InquiryUpsertReq;
import co.kr.customerservice.inquiryAdmin.model.dto.response.InquiryDetailRes;
import co.kr.customerservice.inquiryAdmin.model.dto.response.InquiryListRes;
import org.springframework.data.domain.Pageable;

public interface InquiryAdminManagementService {
    InquiryListRes getInquiryList(Pageable pageable, Long usersIdx);

    InquiryDetailRes updateInquiry(String inquiryCode, InquiryUpsertReq request, Long usersIdx);

    ResultResponse deleteInquiry(String inquiryCode, Long usersIdx);

    InquiryDetailRes addInquiryAnswer(Long userId, String inquiryCode, InquiryAnswerUpsertReq request);

    ResultResponse deleteInquiryAnswer(String inquiryCode, InquiryAnswerDeleteReq request, Long usersIdx);
}
