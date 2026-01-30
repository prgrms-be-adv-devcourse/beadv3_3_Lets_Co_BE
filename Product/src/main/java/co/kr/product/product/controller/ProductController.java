package co.kr.product.product.controller;

import co.kr.product.common.BaseResponse;
import co.kr.product.product.model.dto.request.DeductStockRequest;
import co.kr.product.product.model.dto.request.ProductIdxsRequest;
import co.kr.product.product.model.dto.request.ProductInfoToOrderRequest;
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
     * @param search
     * 상품 목록 검색, ElasticSearch에 연결
     */
    @GetMapping
    public ResponseEntity<ProductListResponse> getProducts(
            @PageableDefault(size = 20) Pageable pageable,
            // @ModelAttribute ProductListRequest requests
            @RequestParam(name = "search") String search) {

        //return productService.getProducts(pageable);

        return ResponseEntity.ok(
                productSearchService.getProductsList(pageable,search));
    }



    /**
     * 상품 상세 조회 (비회원/회원)
     * @param productsCode
     *  상품 코드를 통한 상품 정보 조회
     */
    @GetMapping("/{productsCode}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(
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
    public ProductCheckStockResponse getCheckStock(
            @PathVariable String productsCode){

        return productService.getCheckStock(productsCode);
    }

/*

    @PostMapping("deductStock")
    public void deductStock(
            @RequestBody @Valid DeductStockRequest deductStockRequest
    ) {
        productService.deductStock(deductStockRequest);
    }
*/
    // 상품 재고 차감
    @PostMapping("deductStocks")
    public void deductStockList(
            @RequestBody @Valid List<DeductStockRequest> deductStockRequest
    ) {
        productService.deductStocks(deductStockRequest);
    }


    // order에 보내 줄 상품 정보
    @GetMapping("/{productsIdx}/{optionIdx}")
    public ProductInfoToOrderResponse getProductInfo(
            @PathVariable("productsIdx") Long productsIdx,
            @PathVariable("optionIdx") Long optionIdx
    ) {
        return productService.getProductInfo(productsIdx, optionIdx);
    }

    // order에 보내 줄 상품 정보 리스트
    @GetMapping("/bulk")
    public List<ProductInfoToOrderResponse> getProductInfoList(
            @RequestBody @Valid List<ProductInfoToOrderRequest> requests
    ) {
        return productService.getProductInfoList(requests);
    }


    // board에 보내 줄 상품 정보
    @PostMapping("/byIdx")
    public List<ProductInfoResponse> getProductInfo(
            @RequestBody ProductIdxsRequest request){

        return productService.getProductInfoForBoard(request);
    };

    // 상품의 판매자 단일 조회
    @GetMapping("/sellers/{productsIdx}")
    public ProductSellerResponse getSellerIdx(
            @PathVariable("productsIdx") Long productsIdx){

        return productService.getSellerIdx(productsIdx);
    };

    // 상품의 판매자 다중 조회
    @GetMapping("/sellers")
    public Map<Long, Long> getSellersByProductIdxs(@RequestParam List<Long> productIds) {
        return productService.getSellersByProductIds(productIds);
    }


}



