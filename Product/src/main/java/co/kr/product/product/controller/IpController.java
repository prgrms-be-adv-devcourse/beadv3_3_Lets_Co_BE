package co.kr.product.product.controller;

import co.kr.product.product.controller.swagger.ip.*;
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


    /**
     * 모든 IP 목록 출력. 순서 o
     * @return IP List
     */
    @IpListDocs
    @GetMapping
    public ResponseEntity<List<CategorySortedRes>> getCategory(

    ){
        return ResponseEntity.ok(
                categoryService.getCategory(CategoryType.IP));
    }


    /**
     * 가족 IP 출력
     * @param categoryCode
     * @return CategoryFamilyRes
     */
    @IpFamilyListDocs
    @GetMapping("/{categoryCode}")
    public ResponseEntity<CategoryFamilyRes> getFamilyCategory(
            @PathVariable("categoryCode") String categoryCode
    ) {
        return   ResponseEntity.ok(
                categoryService.getFamilyCategory(categoryCode, CategoryType.IP));}



}
