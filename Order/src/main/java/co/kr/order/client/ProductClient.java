package co.kr.order.client;

import co.kr.order.model.dto.DeductStock;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.request.ProductRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

//@FeignClient(name = "Product")
@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/{productIdx}/{optionIdx}")
    ProductInfo getProduct(
            @PathVariable("productIdx") Long productIdx,
            @PathVariable("optionIdx") Long optionIdx
    );

    @GetMapping("/products/bulk")
    List<ProductInfo> getProductList(@RequestBody List<ProductRequest> productRequests);

    @PostMapping("/products/deductStock")
    void deductStock(@RequestBody DeductStock requests);

    @PostMapping("/products/deductStocks")
    void deductStocks(@RequestBody List<DeductStock> requests);

    /*
     * 정산용: 상품 ID 목록으로 판매자 ID 조회
     * @param productIds 상품 ID 목록
     * @return Map<상품ID, 판매자ID>
     */
    @GetMapping("/products/sellers")
    Map<Long, Long> getSellersByProductIds(@RequestParam List<Long> productIds);

}