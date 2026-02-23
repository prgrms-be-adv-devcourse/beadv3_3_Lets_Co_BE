package co.kr.product.product.service;


import co.kr.product.product.model.dto.request.CategoryUpsertReq;
import co.kr.product.product.model.dto.response.CategoryFamilyRes;
import co.kr.product.product.model.dto.response.CategoryRes;
import co.kr.product.product.model.dto.response.CategorySortedRes;
import co.kr.product.product.model.vo.CategoryType;

import java.util.List;

public interface ProductCategoryService {
    List<CategorySortedRes> getCategory(CategoryType type);

    CategoryRes addCategory(Long usersIdx, CategoryUpsertReq req, CategoryType type);

    String updateCategory(Long usersIdx,String categoryCode ,CategoryUpsertReq req, CategoryType type);

    String deleteCategory(Long usersIdx, String categoryCode, CategoryType type);

    CategoryFamilyRes getFamilyCategory(String categoryCode, CategoryType type);
}
