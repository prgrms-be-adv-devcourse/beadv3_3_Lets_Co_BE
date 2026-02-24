package co.kr.payment.client;

import co.kr.payment.model.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "order-service", path = "/client/orders")
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

    @PostMapping("/success/{orderCode}/{paymentIdx}")
    void successPayment(
            @PathVariable("orderCode") String orderCode,
            @PathVariable("paymentIdx") Long paymentIdx,
            @RequestBody UserInfo userInfo
    );
}
