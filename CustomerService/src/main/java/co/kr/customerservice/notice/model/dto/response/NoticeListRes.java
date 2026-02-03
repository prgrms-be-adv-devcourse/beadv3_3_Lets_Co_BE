package co.kr.customerservice.notice.model.dto.response;

import java.util.List;

public record NoticeListRes(
        String resultCode,
        List<NoticeRes> items
) {


}
