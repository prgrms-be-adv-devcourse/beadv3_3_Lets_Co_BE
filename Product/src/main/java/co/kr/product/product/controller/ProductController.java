package co.kr.product.product.controller;

import co.kr.product.product.model.dto.request.DeductStockReq;
import co.kr.product.product.model.dto.request.ProductIdxsReq;
import co.kr.product.product.model.dto.request.ProductInfoToOrderReq;
import co.kr.product.product.model.dto.request.ProductListReq;
import co.kr.product.product.model.dto.response.*;
import co.kr.product.product.service.ProductSearchService;
import co.kr.product.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;


    /**
     * 상품 목록 조회 (비회원/회원)
     * @param pageable
     * 상품 목록 검색, ElasticSearch에 연결
     */
    @GetMapping
    public ResponseEntity<ProductListRes> getProducts(
            @PageableDefault(size = 20) Pageable pageable,
            // @ModelAttribute ProductListRequest requests
            @RequestBody ProductListReq request) {

        //return productService.getProducts(pageable);

        return ResponseEntity.ok(
                productSearchService.getProductsList(pageable,request));
    }



    /**
     * 상품 상세 조회 (비회원/회원)
     * @param productsCode
     *  상품 코드를 통한 상품 정보 조회
     */
    @GetMapping("/{productsCode}")
    public ResponseEntity<ProductDetailRes> getProductDetail(
            @PathVariable String productsCode) {

        return ResponseEntity.ok(
                productService.getProductDetail(productsCode));
    }


    /**
     * 상품 재고 검사
     * @param productsCode
     * 상품 재고 여부 확인 후 boolean 반환
     */
    @GetMapping("/{productsCode}/checkStock")
    public ProductCheckStockRes getCheckStock(
            @PathVariable String productsCode){

        return productService.getCheckStock(productsCode);
    }



}



