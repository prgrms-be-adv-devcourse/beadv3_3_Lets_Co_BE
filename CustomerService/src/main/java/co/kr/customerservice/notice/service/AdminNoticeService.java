package co.kr.customerservice.notice.service;


import co.kr.customerservice.common.model.dto.response.ResultResponse;
import co.kr.customerservice.notice.model.dto.request.NoticeUpsertReq;
import co.kr.customerservice.notice.model.dto.response.AdminNoticeDetailRes;
import co.kr.customerservice.notice.model.dto.response.NoticeListRes;
import org.springframework.data.domain.Pageable;

public interface AdminNoticeService {
    AdminNoticeDetailRes addNotice(Long userId, NoticeUpsertReq request);

    NoticeListRes getNoticeList(Long userId, Pageable pageable);

    AdminNoticeDetailRes getNoticeDetail(Long userId, String noticeCode);

    AdminNoticeDetailRes updateNotice(Long userId, String noticeCode, NoticeUpsertReq request);

    ResultResponse deleteNotice(Long userId, String noticeCode);
}
