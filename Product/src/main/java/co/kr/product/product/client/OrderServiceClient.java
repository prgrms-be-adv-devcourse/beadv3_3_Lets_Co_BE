package co.kr.product.product.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", url = "http://order-service:8080")
public interface OrderServiceClient {
    @GetMapping("/client/orders/getIdx/{productsCode}")
    Long getOrderItemIdxByCode(@PathVariable("productsCode") String productsCode);
}
