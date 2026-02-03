package co.kr.customerservice.notice.service;


import co.kr.customerservice.common.model.dto.response.ResultResponse;
import co.kr.customerservice.notice.model.dto.request.NoticeUpsertRequest;
import co.kr.customerservice.notice.model.dto.response.AdminNoticeDetailResponse;
import co.kr.customerservice.notice.model.dto.response.NoticeListResponse;
import org.springframework.data.domain.Pageable;

public interface AdminNoticeService {
    AdminNoticeDetailResponse addNotice(Long userId, NoticeUpsertRequest request);

    NoticeListResponse getNoticeList(Long userId, Pageable pageable);

    AdminNoticeDetailResponse getNoticeDetail(Long userId, String noticeCode);

    AdminNoticeDetailResponse updateNotice(Long userId, String noticeCode,NoticeUpsertRequest request);

    ResultResponse deleteNotice(Long userId, String noticeCode);
}
