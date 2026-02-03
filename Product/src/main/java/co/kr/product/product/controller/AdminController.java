package co.kr.product.product.controller;

import co.kr.product.common.vo.UserRole;
import co.kr.product.product.model.dto.request.ProductListReq;
import co.kr.product.product.model.dto.request.UpsertProductReq;
import co.kr.product.product.model.dto.response.ProductDetailRes;
import co.kr.product.product.model.dto.response.ProductListRes;
import co.kr.product.product.model.dto.response.ResultRes;
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
    @DeleteMapping("/products/{code}")
    public ResponseEntity<ResultRes> deleteProduct(

            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("code") String productCode){
        


        productManagerService.deleteProduct(usersIdx, productCode, UserRole.ADMIN);

        return ResponseEntity.ok(new ResultRes("ok"));

    }

}
