package co.kr.costomerservice.model.dto.response;

import java.util.List;

public record NoticeListResponse(
        String resultCode,
        List<NoticeResponse> items
) {


}
