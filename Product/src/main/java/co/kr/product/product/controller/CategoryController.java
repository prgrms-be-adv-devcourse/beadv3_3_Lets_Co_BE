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


    /** 카테고리 추가
     * @param usersIdx
     * @param req
     * @return 카테고리 상세 정보
     */
    @CategoryAddDocs
    @PostMapping
    public ResponseEntity<CategoryRes> addCategory(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @RequestBody CategoryUpsertReq req
            ){

        return ResponseEntity.ok(
                categoryService.addCategory(usersIdx, req, CategoryType.CATEGORY));
    }


    /** 카테고리 수정 (이름 , 부모카테고리)
     * @param usersIdx
     * @param categoryCode
     * @param req
     * @return 카테고리 상세 정보
     */
    @CategoryUpdateDocs
    @PutMapping("/{categoryCode}")
    public ResponseEntity<String> updateCategory(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("categoryCode") String categoryCode,
            @RequestBody CategoryUpsertReq req
    ){


        return ResponseEntity.ok(
                categoryService.updateCategory(usersIdx, categoryCode ,req, CategoryType.CATEGORY));
    }

    /** 카테고리 삭제(하위 카테고리 포함)
     *
     * @param usersIdx
     * @param categoryCode
     * @return
     */
    @CategoryDeleteDocs
    @DeleteMapping("/{categoryCode}")
    public ResponseEntity<String> deleteCategory(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("categoryCode") String categoryCode
    ){

        return ResponseEntity.ok(
                categoryService.deleteCategory(usersIdx, categoryCode, CategoryType.CATEGORY));
    }

}
