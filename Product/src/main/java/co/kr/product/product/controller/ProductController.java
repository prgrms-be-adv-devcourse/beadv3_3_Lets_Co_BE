package co.kr.product.product.controller;

import co.kr.product.product.dto.request.DeductStockRequest;
import co.kr.product.product.dto.request.ProductIdxsRequest;
import co.kr.product.product.dto.request.ProductInfoToOrderRequest;
import co.kr.product.product.dto.response.*;
import co.kr.product.product.service.ProductSearchService;
import co.kr.product.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
     * @param search
     * 상품 목록 검색, ElasticSearch에 연결
     */
    @GetMapping
    public ProductListResponse getProducts(
            @PageableDefault(size = 20) Pageable pageable,
            // @ModelAttribute ProductListRequest requests
            @RequestParam(name = "search") String search) {
        //return productService.getProducts(pageable);
        return productSearchService.getProductsList(pageable,search);
    }



    /**
     * 상품 상세 조회 (비회원/회원)
     * @param productsCode
     *  상품 코드를 통한 상품 정보 조회
     */
    @GetMapping("/{productsCode}")
    public ProductDetailResponse getProductDetail(@PathVariable String productsCode) {
        return productService.getProductDetail(productsCode);
    }


    /**
     * 상품 제고 검사
     * @param productsCode
     * 상품 재고 여부 확인 후 boolean 반환
     */
    @GetMapping("/{productsCode}/checkStock")
    public ProductCheckStockResponse getCheckStock(
            @PathVariable String productsCode){

        return productService.getCheckStock(productsCode);
    }

    @PostMapping("deductStock")
    public void deductStock(
            @RequestBody @Valid DeductStockRequest deductStockRequest
    ) {
        productService.deductStock(deductStockRequest);
    }

    @PostMapping("deductStocks")
    public void deductStockList(
            @RequestBody @Valid List<DeductStockRequest> deductStockRequest
    ) {
        productService.deductStocks(deductStockRequest);
    }

    @GetMapping("/{productsIdx}/{optionIdx}")
    public ProductInfoToOrderResponse getProductInfo(
            @PathVariable("productsIdx") Long productsIdx,
            @PathVariable("optionIdx") Long optionIdx
    ) {
        return productService.getProductInfo(productsIdx, optionIdx);
    }


    @GetMapping("/bulk")
    public List<ProductInfoToOrderResponse> getProductInfoList(
            @RequestBody @Valid List<ProductInfoToOrderRequest> requests
    ) {
        return productService.getProductInfoList(requests);
    }

    @GetMapping("/sellers")
    public Map<Long, Long> getSellersByProductIds(@RequestParam List<Long> productIds) {
        return productService.getSellersByProductIds(productIds);
    }

    @PostMapping("/products/byIdx")
    public List<ProductInfoResponse> getProductInfo(ProductIdxsRequest request){
        return productService.getProductInfoForBoard(request);
    };

    @GetMapping("/products/getSeller/{productsIdx}")
    public ProductSellerResponse getSellerIdx(@PathVariable("productsIdx") Long productsIdx){
        return productService.getSellerIdx(productsIdx);
    };


}



