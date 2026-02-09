package co.kr.customerservice.notice.service;


import co.kr.customerservice.notice.model.dto.response.NoticeDetailRes;
import co.kr.customerservice.notice.model.dto.response.NoticeListRes;
import org.springframework.data.domain.Pageable;

public interface UserNoticeService {
    NoticeListRes getNoticeList(Pageable pageable);

    NoticeDetailRes getNoticeDetail(String noticeCode);
}
