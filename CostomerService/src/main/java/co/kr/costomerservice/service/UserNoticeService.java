package co.kr.costomerservice.service;


import co.kr.costomerservice.model.dto.response.NoticeDetailResponse;
import co.kr.costomerservice.model.dto.response.NoticeListResponse;
import org.springframework.data.domain.Pageable;

public interface UserNoticeService {
    NoticeListResponse getNoticeList(Pageable pageable);

    NoticeDetailResponse getNoticeDetail(String noticeCode);
}
