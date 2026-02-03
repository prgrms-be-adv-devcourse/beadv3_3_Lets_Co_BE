package co.kr.customerservice.notice.service;


import co.kr.customerservice.notice.model.dto.response.NoticeDetailResponse;
import co.kr.customerservice.notice.model.dto.response.NoticeListResponse;
import org.springframework.data.domain.Pageable;

public interface UserNoticeService {
    NoticeListResponse getNoticeList(Pageable pageable);

    NoticeDetailResponse getNoticeDetail(String noticeCode);
}
