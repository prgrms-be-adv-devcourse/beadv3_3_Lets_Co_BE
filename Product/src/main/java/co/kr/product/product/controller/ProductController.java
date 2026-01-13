package co.kr.product.product.controller;

import co.kr.product.product.dto.request.ProductListRequest;
import co.kr.product.product.dto.response.ProductCheckStockResponse;
import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.response.ProductListResponse;
import co.kr.product.product.service.ProductSearchService;
import co.kr.product.product.service.ProductService;
import jakarta.ws.rs.QueryParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;
    // 상품 목록 조회 (비회원/회원)
    @GetMapping
    public ProductListResponse getProducts(
            @PageableDefault(size = 20) Pageable pageable,
            // @ModelAttribute ProductListRequest requests
            @RequestParam(name = "search") String search) {
        //return productService.getProducts(pageable);
        return productSearchService.getProductsList(pageable,search);
    }

    // 상품 상세 조회 (비회원/회원)
    @GetMapping("/{productsCode}")
    public ProductDetailResponse getProductDetail(@PathVariable String productsCode) {
        return productService.getProductDetail(productsCode);
    }

    // 상품 제고 검사
    @GetMapping("/{productsCode}/check_stock")
    public ProductCheckStockResponse getCheckStock(
            @PathVariable String productsCode){

        return productService.getCheckStock(productsCode);
    }

}



