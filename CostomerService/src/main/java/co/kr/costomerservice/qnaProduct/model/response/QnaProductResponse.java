package co.kr.costomerservice.qnaProduct.model.response;

import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;

import java.time.LocalDateTime;

public record QnaProductResponse(

        String code,
        CustomerServiceCategory category,
        CustomerServiceStatus status,
        String title,
        Long viewCount,
        LocalDateTime createdAt,

        String userName
) {

}
