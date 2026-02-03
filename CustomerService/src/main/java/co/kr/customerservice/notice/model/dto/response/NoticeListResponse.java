package co.kr.customerservice.notice.model.dto.response;

import java.util.List;

public record NoticeListResponse(
        String resultCode,
        List<NoticeResponse> items
) {


}
