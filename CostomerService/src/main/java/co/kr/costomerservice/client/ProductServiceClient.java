package co.kr.costomerservice.client;

import co.kr.costomerservice.common.model.dto.request.ProductIdxsRequest;
import co.kr.costomerservice.common.model.dto.response.ProductInfoResponse;
import co.kr.costomerservice.common.model.dto.response.ProductSellerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @FeignClient(name = "Product", url = "http://product-service:8080")
@FeignClient(name = "product-service" , url = "http://product-service:8080", path = "/client/products")
public interface ProductServiceClient {

    @GetMapping("/getInfoByIdx")
    List<ProductInfoResponse> getProductInfo(
            @RequestBody ProductIdxsRequest request);


    @GetMapping("/getSeller/{productsIdx}")
    ProductSellerResponse getSellerIdx(
            @PathVariable("productsIdx") Long productsIdx);


    @GetMapping("/getIdxByCode/{productCode}")
    Long getIdxByCode(
            @PathVariable("productCode") String productCode);


}
