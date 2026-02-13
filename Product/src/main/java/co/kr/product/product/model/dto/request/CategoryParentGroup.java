package co.kr.product.product.model.dto.request;

import co.kr.product.product.model.dto.response.CategoryInfoRes;
import co.kr.product.product.model.entity.ProductCategoryEntity;

import java.util.List;

public record CategoryParentGroup(
        List<CategoryInfoRes> categoryParents,
        List<CategoryInfoRes> ipParents
) {
}
