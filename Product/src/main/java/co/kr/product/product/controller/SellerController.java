package co.kr.product.product.controller;

import co.kr.product.product.dto.request.ProductListRequest;
import co.kr.product.product.dto.request.UpsertProductRequest;
import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.response.ProductListResponse;
import co.kr.product.product.dto.response.ResultResponse;
import co.kr.product.product.service.ProductManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/seller")
public class SellerController {

    private final ProductManagerService productManagerService;

    /**
     * // @param accountCode
     * @param pageable
     * @param requests
     * @return 판매자가 올린 상품 리스트 반환
     */
    @GetMapping("/products")
    public ResponseEntity<ProductListResponse> getLists(
            @PageableDefault Pageable pageable,
            @ModelAttribute ProductListRequest requests
            ){
        String accountCode="test";
        ProductListResponse result = productManagerService.getListsBySeller(accountCode, pageable, requests);

        return ResponseEntity.ok(result);
    }


    /**
     * 상품 등록(판매자)
     * @param accountCode
     * @param request
     * @return 등록 된 상품의 상세 정보
     */
    @PostMapping("/products")
    public  ResponseEntity<ProductDetailResponse> addProduct(
            //`@AuthenticationPrincipal(expression = "accountCode")`
            String accountCode, // 임시
            @RequestBody UpsertProductRequest request

            ){
        ProductDetailResponse result = productManagerService.addProduct(accountCode,request);

        return ResponseEntity.ok(result);
    }

    /**
     * 상품 상세 정보(판매자)
     * @param accountCode
     * @param productCode
     * @return 상품 상세 정보
     */
    @GetMapping("/products/{code}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(
            String accountCode,
            @PathVariable("code") String productCode
    ){
        ProductDetailResponse result = productManagerService.getManagerProductDetail(accountCode,productCode);

        return ResponseEntity.ok(result);
    }

    /**
     * 상품 정보 수정(판매자)
     * @param accountCode
     * @param productCode
     * @param request
     * @return 상품 상세 정보
     */
    @PutMapping("/products/{code}")
    public ResponseEntity<ProductDetailResponse> updateProduct(
            String accountCode,
            @PathVariable("code") String productCode,
            @RequestBody UpsertProductRequest request
    ){
        ProductDetailResponse result = productManagerService.updateProduct(accountCode,productCode, request);
        return ResponseEntity.ok(result);
    }

    /**
     * 상품 제거(판매자)
     * @param accountCode
     * @param productCode
     * @return resultCode
     */
    @DeleteMapping("/products/{code}")
    public ResponseEntity<ResultResponse> deleteProduct(
            String accountCode,
            @PathVariable("code") String productCode
    ){
        productManagerService.deleteProduct(accountCode,productCode);
        return ResponseEntity.ok(new ResultResponse("Success"));
    }
}
