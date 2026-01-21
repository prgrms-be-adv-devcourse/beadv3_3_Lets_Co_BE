package co.kr.costomerservice.qnaProduct.model;

import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;

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
