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
@RequestMapping("/admin")
public class AdminController {

    private final ProductManagerService productManagerService;

    @GetMapping("/products")
    public ResponseEntity<ProductListReponse> getProductList(
            @PageableDefault Pageable pageable,
            @ModelAttribute ProductListRequest requests
            ){

        log.info(requests.search(),requests.category());
        return ResponseEntity.ok(null);

    }

    @GetMapping("/products/{code}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(
            //`@AuthenticationPrincipal(expression = "accountCode")`
            String accountCode,
            @PathVariable("code") String productCode){

        ProductDetailResponse result = productManagerService.getProductDetail(accountCode, productCode);
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
