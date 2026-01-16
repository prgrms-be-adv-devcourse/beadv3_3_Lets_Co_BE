package co.kr.costomerservice.qnaProduct.model.request;

import co.kr.costomerservice.common.vo.CustomerServiceCategory;

public record QnaProductUpsertRequest(
        Long productsIdx,
        CustomerServiceCategory category,
        String title,
        boolean isPrivate,

        String detailCode, // 수정용
        
        String name,
        String parentCode,
        String content
        ) {
}
