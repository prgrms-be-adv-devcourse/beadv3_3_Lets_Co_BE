package co.kr.order.client;

import co.kr.order.model.dto.ProductInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// 유레카 서버 사용 안하면, url = "${product.url} 직접 설정
@FeignClient(name = "Product")
public interface ProductClient {

    @GetMapping("/{productId}")
    ProductInfo getProductById(@PathVariable("productId") Long productId);
}