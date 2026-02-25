package co.kr.product.product.controller;

import co.kr.product.common.vo.UserRole;
import co.kr.product.product.controller.swagger.category.CategoryAddDocs;
import co.kr.product.product.controller.swagger.category.CategoryDeleteDocs;
import co.kr.product.product.controller.swagger.category.CategoryUpdateDocs;
import co.kr.product.product.controller.swagger.ip.IpAddDocs;
import co.kr.product.product.controller.swagger.ip.IpDeleteDocs;
import co.kr.product.product.controller.swagger.ip.IpUpdateDocs;
import co.kr.product.product.controller.swagger.product.AdminProductListDocs;
import co.kr.product.product.controller.swagger.product.ManagerProductDetailDocs;
import co.kr.product.product.controller.swagger.product.ProductDeleteDocs;
import co.kr.product.product.controller.swagger.product.ProductUpdateDocs;
import co.kr.product.product.model.dto.request.CategoryUpsertReq;
import co.kr.product.product.model.dto.request.ProductListReq;
import co.kr.product.product.model.dto.request.UpsertProductReq;
import co.kr.product.product.model.dto.response.CategoryRes;
import co.kr.product.product.model.dto.response.ProductDetailRes;
import co.kr.product.product.model.dto.response.ProductListRes;
import co.kr.product.product.model.dto.response.ResultRes;
import co.kr.product.product.model.vo.CategoryType;
import co.kr.product.product.service.ProductCategoryService;
import co.kr.product.product.service.ProductManagerService;
import co.kr.product.product.service.ProductSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final ProductManagerService productManagerService;
    private final ProductSearchService productSearchService;
    private final ProductCategoryService categoryService;

    // Elastic 연결 테스트용 메서드
/*    @GetMapping("/test")
    public ResponseEntity<List<ProductDocument>> testElastic(){
        List<ProductDocument> test =  productSearchService.search();
        return ResponseEntity.ok(test);
    }*/

    /**
     * 상품 목록 조회 (관리자용)
     * @param pageable  page,size,sort
     * @param requests  search
     * @return 상품 리스트
     */
    @AdminProductListDocs
    @GetMapping("/products")
    public ResponseEntity<ProductListRes> getProductList(
            @PageableDefault Pageable pageable,
            @ModelAttribute @Valid ProductListReq requests
            ){


        return ResponseEntity.ok(
                productSearchService.getProductsList(pageable,requests));

    }

    /**
     * 상품 상세 조회(관리자용)
     * // @param accountCode
     * @param productCode
     * @return 상품 상세 정보
     */
    @ManagerProductDetailDocs
    @GetMapping("/products/{code}")
    public ResponseEntity<ProductDetailRes> getProductDetail(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("code") String productCode){


        return ResponseEntity.ok(
                productManagerService.getManagerProductDetail(usersIdx, productCode));
    }

    /**
     * 상품 code를 통한 상품 정보 수정(관리자)
     * // @param accountCode
     * @param request
     * @param productCode
     * @return 상품 상세 정보
     */
    @ProductUpdateDocs
    @PutMapping("/products/{code}")
    public ResponseEntity<ProductDetailRes> updateProduct(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("code") String productCode,
            @RequestBody @Valid UpsertProductReq request
            ){

        return ResponseEntity.ok(
                productManagerService.updateProduct(usersIdx, productCode, request, UserRole.ADMIN));

    }

    /**
     * 상품 코드를 통한 상품 삭제 (관리자)
     * // @param accountCode
     * @param productCode
     * @return resultCode
     */
    @ProductDeleteDocs
    @DeleteMapping("/products/{code}")
    public ResponseEntity<ResultRes> deleteProduct(

            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("code") String productCode){
        


        productManagerService.deleteProduct(usersIdx, productCode, UserRole.ADMIN);

        return ResponseEntity.ok(new ResultRes("ok"));

    }

    //--------------------------------------------------------------------
    // 카테고리/ IP 관련
    //--------------------------------------------------------------------

    /** 카테고리 추가
     * @param usersIdx
     * @param req
     * @return 카테고리 상세 정보
     */
    @CategoryAddDocs
    @PostMapping("/products/category")
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
    @PutMapping("/products/category/{categoryCode}")
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
    @DeleteMapping("/products/category/{categoryCode}")
    public ResponseEntity<String> deleteCategory(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("categoryCode") String categoryCode
    ){

        return ResponseEntity.ok(
                categoryService.deleteCategory(usersIdx, categoryCode, CategoryType.CATEGORY));
    }


    /**
     * IP 추가
     * @param usersIdx
     * @param req
     * @return IP 상세 정보
     */
    @IpAddDocs
    @PostMapping("/products/ip")
    public ResponseEntity<CategoryRes> addIP(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @RequestBody CategoryUpsertReq req
    ){

        return ResponseEntity.ok(
                categoryService.addCategory(usersIdx, req, CategoryType.IP));
    }



    /** IP 수정 (이름 , 부모IP)
     * @param usersIdx
     * @param categoryCode
     * @param req
     * @return IP 상세 정보
     */
    @IpUpdateDocs
    @PutMapping("/products/ip/{categoryCode}")
    public ResponseEntity<String> updateIP(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("categoryCode") String categoryCode,
            @RequestBody CategoryUpsertReq req
    ){

        return ResponseEntity.ok(
                categoryService.updateCategory(usersIdx, categoryCode ,req, CategoryType.IP));
    }


    /** IP 삭제(하위 IP 포함)
     * @param usersIdx
     * @param categoryCode
     * @return
     */
    @IpDeleteDocs
    @DeleteMapping("/products/ip/{categoryCode}")
    public ResponseEntity<String> deleteIP(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("categoryCode") String categoryCode
    ){

        return ResponseEntity.ok(
                categoryService.deleteCategory(usersIdx, categoryCode, CategoryType.IP));
    }


}
