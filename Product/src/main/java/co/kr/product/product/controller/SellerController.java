package co.kr.product.product.controller;

import co.kr.product.product.dto.request.ProductListRequest;
import co.kr.product.product.dto.request.UpsertProductRequest;
import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.response.ProductListResponse;
import co.kr.product.product.dto.response.ResultResponse;
import co.kr.product.product.service.ProductManagerService;
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
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PageableDefault Pageable pageable,
            @ModelAttribute @Valid ProductListRequest requests
            ){
        ProductListResponse result = productManagerService.getListsBySeller(usersIdx, pageable, requests);

        return ResponseEntity.ok(result);
    }


    /**
     * 상품 등록(판매자)
     * @param usersIdx
     * @param request
     * @return 등록 된 상품의 상세 정보
     */
    @PostMapping("/products")
    public  ResponseEntity<ProductDetailResponse> addProduct(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @RequestBody @Valid UpsertProductRequest request

            ){
        ProductDetailResponse result = productManagerService.addProduct(usersIdx,request);

        return ResponseEntity.ok(result);
    }

    /**
     * 상품 상세 정보(판매자)
     * @param usersIdx
     * @param productCode
     * @return 상품 상세 정보
     */
    @GetMapping("/products/{code}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("code") String productCode
    ){
        ProductDetailResponse result = productManagerService.getManagerProductDetail(usersIdx,productCode);

        return ResponseEntity.ok(result);
    }

    /**
     * 상품 정보 수정(판매자)
     * @param usersIdx
     * @param productCode
     * @param request
     * @return 상품 상세 정보
     */
    @PutMapping("/products/{code}")
    public ResponseEntity<ProductDetailResponse> updateProduct(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("code") String productCode,
            @RequestBody @Valid UpsertProductRequest request
    ){
        ProductDetailResponse result = productManagerService.updateProduct(usersIdx,productCode, request);
        return ResponseEntity.ok(result);
    }

    /**
     * 상품 제거(판매자)
     * @param usersIdx
     * @param productCode
     * @return resultCode
     */
    @DeleteMapping("/products/{code}")
    public ResponseEntity<ResultResponse> deleteProduct(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("code") String productCode
    ){
        productManagerService.deleteProduct(usersIdx,productCode);
        return ResponseEntity.ok(new ResultResponse("ok"));
    }
}
