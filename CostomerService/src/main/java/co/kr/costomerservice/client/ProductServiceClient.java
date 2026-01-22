package co.kr.costomerservice.client;

import co.kr.costomerservice.common.dto.request.ProductIdxsRequest;
import co.kr.costomerservice.common.dto.response.ProductInfoResponse;
import co.kr.costomerservice.common.dto.response.ProductSellerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

// @FeignClient(name = "Product", url = "http://product-service:8080")
@FeignClient(name = "product-service" , url = "http://product-service:8080")
public interface ProductServiceClient {
    @PostMapping("/product/byIdx")
    List<ProductInfoResponse> getProductInfo(ProductIdxsRequest request);

    @GetMapping("/product/getSeller/{productsIdx}")
    ProductSellerResponse getSellerIdx(@PathVariable("productsIdx") Long productsIdx);

}