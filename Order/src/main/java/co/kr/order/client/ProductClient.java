package co.kr.order.client;

import co.kr.order.model.dto.request.ClientProductReq;
import co.kr.order.model.dto.request.DeductStockReq;
import co.kr.order.model.dto.response.ClientProductRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "product-service", path = "/client/products", url = "http://product-service:8080")
public interface ProductClient {

    @GetMapping("/{productCode}/{optionCode}")
    ClientProductRes getProduct(
            @PathVariable("productCode") String productCode,
            @PathVariable("optionCode") String optionCode
    );

    @PostMapping("/bulk")
    List<ClientProductRes> getProductList(@RequestBody List<ClientProductReq> productRequest);

    @PostMapping("/deductStocks")
    void deductStocks(@RequestBody List<DeductStockReq> deductStockRequest);

    /*
     * 정산용: 상품 ID 목록으로 판매자 ID 조회
     * @param productIds 상품 ID 목록
     * @return Map<상품ID, 판매자ID>
     */
    @GetMapping("/sellers")
    Map<Long, Long> getSellersByProductIds(@RequestParam List<Long> productIds);
}