package co.kr.order.client;

import co.kr.order.model.dto.request.ClientProductReq;
import co.kr.order.model.dto.response.ClientProductRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/*
 * FeignClient
 * Product-Service에 API 요청
 */
@FeignClient(name = "product-service", path = "/client/products", url = "http://product-service:8080")
public interface ProductClient {

    /*
     * 단일 상품 정보 요청 (GET)
     * @param productCode: 상품 코드
     * @param optionCode: 상품 옵션 코드
     */
    @GetMapping("/{productCode}/{optionCode}")
    ClientProductRes getProduct(
            @PathVariable("productCode") String productCode,
            @PathVariable("optionCode") String optionCode
    );

    /*
     * 상품 리스트 정보 요청 (POST)
     * @param productRequest: 상품 리스트 요청 정보 (productIdx, optionIdx)
     */
    @PostMapping("/bulk")
    List<ClientProductRes> getProductList(@RequestBody List<ClientProductReq> productRequest);

    /*
     * 정산용: 상품 ID 목록으로 판매자 ID 조회
     * @param productIds 상품 ID 목록
     * @return Map<상품ID, 판매자ID>
     */
    @GetMapping("/sellers")
    Map<Long, Long> getSellersByProductIds(@RequestParam List<Long> productIds);
}