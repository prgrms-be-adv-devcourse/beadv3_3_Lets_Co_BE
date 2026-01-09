package co.kr.product.seller.controller;

import co.kr.product.seller.model.dto.*;
import co.kr.product.seller.service.ProductManagerService;
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

    @GetMapping("/products")
    public ResponseEntity<ProductListReponse> getLists(
            @PageableDefault Pageable pageable,
            @ModelAttribute ProductListRequest requests
            ){

        ProductListReponse result = productManagerService.getLists(pageable, requests);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/products")
    public  ResponseEntity<ProductDetailResponse> addProduct(
            //`@AuthenticationPrincipal(expression = "accountCode")`
            String accountCode, // 임시
            @RequestBody UpsertProductRequest request

            ){
        ProductDetailResponse result = productManagerService.addProduct(accountCode,request);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/products/{code}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(
            String accountCode,
            @PathVariable("code") String productCode
    ){
        ProductDetailResponse result = productManagerService.getProductDetail(accountCode,productCode);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/products/{code}")
    public ResponseEntity<ProductDetailResponse> updateProduct(
            String accountCode,
            @PathVariable("code") String productCode,
            @RequestBody UpsertProductRequest request
    ){
        ProductDetailResponse result = productManagerService.updateProduct(accountCode,productCode, request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/products/{code}")
    public ResponseEntity<ResultResponse> deleteProduct(
            String accountCode,
            @PathVariable("code") String productCode
    ){
        productManagerService.deleteProduct(accountCode,productCode);
        return ResponseEntity.ok(new ResultResponse("Success"));
    }
}
