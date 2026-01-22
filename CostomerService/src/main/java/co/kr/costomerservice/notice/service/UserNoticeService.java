package co.kr.costomerservice.notice.service;


import co.kr.costomerservice.notice.model.dto.response.NoticeDetailResponse;
import co.kr.costomerservice.notice.model.dto.response.NoticeListResponse;
import org.springframework.data.domain.Pageable;

public interface UserNoticeService {
    NoticeListResponse getNoticeList(Pageable pageable);

    NoticeDetailResponse getNoticeDetail(String noticeCode);
}
