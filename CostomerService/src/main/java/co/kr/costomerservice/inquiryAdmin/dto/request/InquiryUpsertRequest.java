package co.kr.costomerservice.inquiryAdmin.dto.request;

import co.kr.costomerservice.common.vo.CustomerServiceCategory;

public record InquiryUpsertRequest(

        String detailCode,

        // CustomerServiceCategory 전부 가능
        CustomerServiceCategory category,
        String title,
        String content,
        boolean isPrivate
        ) {

}
