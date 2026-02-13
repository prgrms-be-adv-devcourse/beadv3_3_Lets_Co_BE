package co.kr.payment.client;

import co.kr.payment.model.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", path = "/client/orders" , url = "http://order-service:8080")
public interface OrderClient {

    @PostMapping("/{orderCode}/status")
    void updateOrderStatus(
            @PathVariable String orderCode,
            @RequestParam String status
    );

    @GetMapping("/{orderCode}/idx")
    Long getOrderIdx(
            @PathVariable String orderCode
    );

    @PostMapping("/fail/{orderCode}")
    void failPayment(
            @PathVariable String orderCode
    );

    @PostMapping("/success/{orderCode}")
    void successPayment(
            @PathVariable String orderCode,
            @RequestParam UserInfo userInfo
    );
}
