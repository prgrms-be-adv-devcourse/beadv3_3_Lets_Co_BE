package co.kr.product.product.controller;

import co.kr.product.product.model.dto.request.DeductStockReq;
import co.kr.product.product.model.dto.request.ProductIdxsReq;
import co.kr.product.product.model.dto.request.ProductInfoToOrderReq;
import co.kr.product.product.model.dto.response.ProductInfoRes;
import co.kr.product.product.model.dto.response.ProductInfoToOrderRes;
import co.kr.product.product.model.dto.response.ProductSellerRes;
import co.kr.product.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client/product")
public class ClientController {

    private final ProductService productService;

    /*

    @PostMapping("deductStock")
    public void deductStock(
            @RequestBody @Valid DeductStockRequest deductStockRequest
    ) {
        productService.deductStock(deductStockRequest);
    }
*/

    // 상품 재고 차감
    @PostMapping("/deductStocks")
    public void deductStockList(
            @RequestBody @Valid List<DeductStockReq> deductStockReq
    ) {
        productService.deductStocks(deductStockReq);
    }


    // order에 보내 줄 상품 정보
    @GetMapping("/{productsIdx}/{optionIdx}")
    public ProductInfoToOrderRes getProductInfo(
            @PathVariable("productsIdx") Long productsIdx,
            @PathVariable("optionIdx") Long optionIdx
    ) {
        return productService.getProductInfo(productsIdx, optionIdx);
    }

    // order에 보내 줄 상품 정보 리스트
    @GetMapping("/bulk")
    public List<ProductInfoToOrderRes> getProductInfoList(
            @RequestBody @Valid List<ProductInfoToOrderReq> requests
    ) {
        return productService.getProductInfoList(requests);
    }


    @GetMapping("/getIdxByCode/{productCode}")
    Long getIdxByCode(
            @PathVariable("productCode") String productCode){

        return productService.getProductIdxByCode(productCode);
    }



    // board에 보내 줄 상품 정보
    @GetMapping("/getInfoByIdx")
    public List<ProductInfoRes> getProductInfo(
            @RequestBody ProductIdxsReq request){

        return productService.getProductInfoForBoard(request);
    };

    // 상품의 판매자 단일 조회
    @GetMapping("/sellers/{productsIdx}")
    public ProductSellerRes getSellerIdx(
            @PathVariable("productsIdx") Long productsIdx){

        return productService.getSellerIdx(productsIdx);
    };

    // 상품의 판매자 다중 조회
    @GetMapping("/sellers")
    public Map<Long, Long> getSellersByProductIdxs(@RequestParam List<Long> productIds) {
        return productService.getSellersByProductIds(productIds);
    }


}
