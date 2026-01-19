package co.kr.costomerservice.inquiryAdmin.service;

import co.kr.costomerservice.common.dto.response.ResultResponse;
import co.kr.costomerservice.inquiryAdmin.dto.request.InquiryAnswerDeleteRequest;
import co.kr.costomerservice.inquiryAdmin.dto.request.InquiryAnswerUpsertRequest;
import co.kr.costomerservice.inquiryAdmin.dto.request.InquiryUpsertRequest;
import co.kr.costomerservice.inquiryAdmin.dto.response.InquiryDetailResponse;
import co.kr.costomerservice.inquiryAdmin.dto.response.InquiryListResponse;
import org.springframework.data.domain.Pageable;

public interface InquiryAdminManagementService {
    InquiryListResponse getInquiryList(Pageable pageable, Long usersIdx);

    InquiryDetailResponse updateInquiry(String inquiryCode, InquiryUpsertRequest request, Long usersIdx);

    ResultResponse deleteInquiry(String inquiryCode, Long usersIdx);

    InquiryDetailResponse addInquiryAnswer(Long userId, String inquiryCode, InquiryAnswerUpsertRequest request);

    ResultResponse deleteInquiryAnswer(String inquiryCode, InquiryAnswerDeleteRequest request, Long usersIdx);
}
