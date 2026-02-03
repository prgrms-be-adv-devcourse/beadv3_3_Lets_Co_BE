package co.kr.customerservice.qnaProduct.model.response;

import co.kr.customerservice.common.model.vo.CustomerServiceCategory;
import co.kr.customerservice.common.model.vo.CustomerServiceStatus;

import java.time.LocalDateTime;

public record QnaProductRes(

        String code,
        CustomerServiceCategory category,
        CustomerServiceStatus status,
        String title,
        Long viewCount,
        LocalDateTime createdAt,

        String userName

) {

}
