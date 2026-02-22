package co.kr.product.product.controller;

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
@RequestMapping("/products/ip")
public class IpController {

    private final ProductCategoryService categoryService;

    // 모든 IP 목록 출력. 순서 o
    @GetMapping
    public ResponseEntity<List<CategorySortedRes>> getCategory(

    ){
        return ResponseEntity.ok(
                categoryService.getCategory(CategoryType.IP));
    }

    // 상위 IP 출력
    @GetMapping("/{categoryCode}")
    public ResponseEntity<CategoryFamilyRes> getFamilyCategory(
            @PathVariable("categoryCode") String categoryCode
    ) {
        return   ResponseEntity.ok(
                categoryService.getFamilyCategory(categoryCode, CategoryType.IP));}


    // IP 추가
    @PostMapping
    public ResponseEntity<CategoryRes> addCategory(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @RequestBody CategoryUpsertReq req
            ){

        return ResponseEntity.ok(
                categoryService.addCategory(usersIdx, req, CategoryType.IP));
    }


    // IP 수정 (이름 , 부모IP)
    @PutMapping("/{categoryCode}")
    public ResponseEntity<String> updateCategory(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("categoryCode") String categoryCode,
            @RequestBody CategoryUpsertReq req
    ){

        return ResponseEntity.ok(
                categoryService.updateCategory(usersIdx, categoryCode ,req, CategoryType.IP));
    }

    // IP 삭제(하위 IP 포함)
    @DeleteMapping("/{categoryCode}")
    public ResponseEntity<String> deleteCategory(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("categoryCode") String categoryCode
    ){

        return ResponseEntity.ok(
                categoryService.deleteCategory(usersIdx, categoryCode, CategoryType.IP));
    }

}
