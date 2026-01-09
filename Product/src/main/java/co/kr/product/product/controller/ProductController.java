package co.kr.product.product.controller;

import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.response.ProductListResponse;
import co.kr.product.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // 상품 목록 조회 (비회원/회원)
    @GetMapping
    public ProductListResponse getProducts(@PageableDefault(size = 20) Pageable pageable) {
        return productService.getProducts(pageable);
    }

    // 상품 상세 조회 (비회원/회원)
    @GetMapping("/{productsIdx}")
    public ProductDetailResponse getProductDetail(@PathVariable Long productsIdx) {
        return productService.getProductDetail(productsIdx);
    }
}



