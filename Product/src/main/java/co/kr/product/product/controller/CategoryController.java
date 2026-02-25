package co.kr.product.product.controller;

import co.kr.product.product.controller.swagger.category.*;
import co.kr.product.product.model.dto.request.CategoryUpsertReq;
import co.kr.product.product.model.dto.response.CategoryFamilyRes;
import co.kr.product.product.model.dto.response.CategoryRes;
import co.kr.product.product.model.dto.response.CategorySortedRes;
import co.kr.product.product.model.vo.CategoryType;
import co.kr.product.product.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products/category")
public class CategoryController {

    private final ProductCategoryService categoryService;


    /** 모든 카테고리 목록 출력
     * 순서 ㅇ, 정렬 ㅇ
     * @return 카테고리 전체 목록
     */
    @CategoryListDocs
    @GetMapping
    public ResponseEntity<List<CategorySortedRes>> getCategory(

    ){
        return ResponseEntity.ok(
                categoryService.getCategory(CategoryType.CATEGORY));
    }

    /** 가족 카테고리 출력
     * @param categoryCode
     * @return CategoryFamilyRes
     */
    @CategoryFamilyListDocs
    @GetMapping("/{categoryCode}")
    public ResponseEntity<CategoryFamilyRes> getFamilyCategory(
            @PathVariable("categoryCode") String categoryCode
    ) {
        return   ResponseEntity.ok(
                categoryService.getFamilyCategory(categoryCode, CategoryType.CATEGORY));}


}
