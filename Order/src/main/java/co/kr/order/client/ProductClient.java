package co.kr.order.client;

import co.kr.order.model.dto.request.CheckStockRequest;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.request.ProductRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

// 유레카 서버 사용 안하면, url = "${product.url} 직접 설정
@FeignClient(name = "Product")
public interface ProductClient {

    /*
     * @param productIdx: 상품 id
     * @param optionIdx: 상품옵션 id
     * ProductInfo(상품 정보)를 가져오기 위한 Product-Service간의 동기 통신
     */
    @GetMapping("/products")
    ProductInfo getProduct(ProductRequest  productRequest);

    /*
     * @param productRequests: productIdx와 optionIdx를 list로 한번에 보내기 위한 Dto
     * ProductInfo(상품 정보)를 가져오기 위한 Product-Service간의 동기 통신 (리스트로 - N+1)
     */
    @GetMapping("/products/bulk")
    List<ProductInfo> getProductList(@RequestBody List<ProductRequest> productRequests);

    @PostMapping("/products/check-stock")
    void checkStock(@RequestBody CheckStockRequest requests);

    @PostMapping("/products/check-stocks")
    void checkStocks(@RequestBody List<CheckStockRequest> requests);
}