package co.kr.product.product.controller;

import co.kr.product.product.document.ProductDocument;
import co.kr.product.product.dto.request.ProductListRequest;
import co.kr.product.product.dto.request.UpsertProductRequest;
import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.response.ProductListResponse;
import co.kr.product.product.dto.response.ResultResponse;
import co.kr.product.product.service.ProductManagerService;
import co.kr.product.product.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final ProductManagerService productManagerService;
    private final ProductSearchService productSearchService;
    @GetMapping("/test")
    public ResponseEntity<List<ProductDocument>> testElastic(){
        List<ProductDocument> test =  productSearchService.search();
        return ResponseEntity.ok(test);
    }

    @GetMapping("/products")
    public ResponseEntity<ProductListResponse> getProductList(
            @PageableDefault Pageable pageable,
            @ModelAttribute ProductListRequest requests
            ){

        return ResponseEntity.ok(null);

    }

    @GetMapping("/products/{code}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(
            //`@AuthenticationPrincipal(expression = "accountCode")`
            String accountCode,
            @PathVariable("code") String productCode){

        ProductDetailResponse result = productManagerService.getManagerProductDetail(accountCode, productCode);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/products/{code}")
    public ResponseEntity<ProductDetailResponse> updateProduct(
            //`@AuthenticationPrincipal(expression = "accountCode")`
            String accountCode,
            @RequestBody UpsertProductRequest request,
            @PathVariable("code") String productCode){

        ProductDetailResponse result = productManagerService.updateProduct(accountCode, productCode, request);

        return ResponseEntity.ok(result);

    }

    @DeleteMapping("/products/{code}")
    public ResponseEntity<ResultResponse> deleteProduct(
            String accountCode,
            @PathVariable("code") String productCode){
        productManagerService.deleteProduct(accountCode, productCode);

        return ResponseEntity.ok(new ResultResponse("Success"));

    }

}
