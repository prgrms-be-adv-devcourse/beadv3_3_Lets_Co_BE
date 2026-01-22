package co.kr.product.product.controller;

import co.kr.product.product.dto.request.ProductListRequest;
import co.kr.product.product.dto.request.UpsertProductRequest;
import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.response.ProductListResponse;
import co.kr.product.product.dto.response.ResultResponse;
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
    public ResponseEntity<ProductListResponse> getProductList(
            @PageableDefault Pageable pageable,
            @ModelAttribute @Valid ProductListRequest requests
            ){


        return ResponseEntity.ok(productSearchService.getProductsList(pageable,requests.search()));

    }

    /**
     * 상품 상세 조회(관리자용)
     * // @param accountCode
     * @param productCode
     * @return 상품 상세 정보
     */
    @GetMapping("/products/{code}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("code") String productCode){

        
        ProductDetailResponse result = productManagerService.getManagerProductDetail(usersIdx, productCode);
        return ResponseEntity.ok(result);
    }

    /**
     * 상품 code를 통한 상품 정보 수정(관리자)
     * // @param accountCode
     * @param request
     * @param productCode
     * @return 상품 상세 정보
     */

    @PutMapping("/products/{code}")
    public ResponseEntity<ProductDetailResponse> updateProduct(
            @RequestHeader("X-USERS-IDX") Long usersIdx,

            @RequestBody @Valid UpsertProductRequest request,
            @PathVariable("code") String productCode){

        ProductDetailResponse result = productManagerService.updateProduct(usersIdx, productCode, request);

        return ResponseEntity.ok(result);

    }

    /**
     * 상품 코드를 통한 상품 삭제 (관리자)
     * // @param accountCode
     * @param productCode
     * @return resultCode
     */
    @DeleteMapping("/products/{code}")
    public ResponseEntity<ResultResponse> deleteProduct(

            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("code") String productCode){
        


        productManagerService.deleteProduct(usersIdx, productCode);

        return ResponseEntity.ok(new ResultResponse("ok"));

    }

}
