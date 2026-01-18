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

@FeignClient(name = "Product")
public interface ProductClient {

    /*
     * @param productRequest (productIdx, optionIdx)
     * ProductInfo(상품 정보)를 가져오기 위한 Product-Service간의 동기 통신
     */
    @GetMapping("/products/{productIdx}/{optionIdx}")
    ProductInfo getProduct(
            @PathVariable("productIdx") Long productIdx,
            @PathVariable("optionIdx") Long optionIdx
    );

    /*
     * @param productRequests: productIdx와 optionIdx를 list로 한번에 보내기 위한 Dto
     * ProductInfo(상품 정보)를 가져오기 위한 Product-Service간의 동기 통신 (리스트로 - N+1 방지)
     */
    @GetMapping("/products/bulk")
    List<ProductInfo> getProductList(@RequestBody List<ProductRequest> productRequests);

    /*
     * @param requests : productIdx, optionIdx, quantity
     * 주문 후 상품 재고 관리를 위한 quantity 전송
     */
    @PostMapping("/products/deductStock")
    void deductStock(@RequestBody DeductStock requests);

    /*
     * 정산용: 상품 ID 목록으로 판매자 ID 조회
     * @param productIds 상품 ID 목록
     * @return Map<상품ID, 판매자ID>
     */
    @GetMapping("/products/sellers")
    Map<Long, Long> getSellersByProductIds(@RequestParam List<Long> productIds);
    @PostMapping("/products/deductStocks")
    void deductStocks(@RequestBody List<DeductStock> requests);
}