package co.kr.customerservice.qnaProduct.model.response;

import co.kr.customerservice.common.model.vo.CustomerServiceCategory;
import co.kr.customerservice.common.model.vo.CustomerServiceStatus;

import java.time.LocalDateTime;

public record QnaAndProductInfoResponse(

        String code,
        CustomerServiceCategory category,
        CustomerServiceStatus status,
        String title,
        Long viewCount,
        LocalDateTime createdAt,

        String userName,

        // 간단한 상품 정보
        String productCode,
        String name,

        String imageUrl
) {

}
