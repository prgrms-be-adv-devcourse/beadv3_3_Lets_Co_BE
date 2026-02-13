package co.kr.product.product.model.dto.response;

import java.util.List;

public record CategoryFamilyRes(
        List<CategoryInfoRes> parentCategory,
        List<CategoryInfoRes> childCategory
) {
}
