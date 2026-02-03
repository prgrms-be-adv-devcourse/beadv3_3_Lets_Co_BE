package co.kr.customerservice.qnaProduct.model;

import co.kr.customerservice.common.model.vo.CustomerServiceCategory;
import co.kr.customerservice.common.model.vo.CustomerServiceStatus;

import java.time.LocalDateTime;

public record QnaProductQuestionDTO(
        String code,
        CustomerServiceCategory category,
        CustomerServiceStatus status,
        String title,
        String userName,
        Long viewCount,
        LocalDateTime createdAt,
        Boolean isPrivate,

        Long usersIdx,
        Long productsIdx
) {
}
