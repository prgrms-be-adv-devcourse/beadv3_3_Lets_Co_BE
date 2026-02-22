package co.kr.product.product.model.dto.response;

import java.util.List;

public record CategorySortedRes(
        String categoryCode,
        String categoryName,
        int level,

        List<CategorySortedRes> children

) {
}
