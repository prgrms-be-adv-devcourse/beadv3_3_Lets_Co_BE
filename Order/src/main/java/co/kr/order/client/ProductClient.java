package co.kr.order.client;

import co.kr.order.model.dto.request.DeductStock;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.request.ProductRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

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
    void checkStock(@RequestBody DeductStock requests);

    /*
     * @param requests : productIdx, optionIdx, quantity
     * 주문 후 상품 재고 관리를 위한 quantity 리스트 전송
     */
    @PostMapping("/products/deductStocks")
    void checkStocks(@RequestBody List<DeductStock> requests);
}