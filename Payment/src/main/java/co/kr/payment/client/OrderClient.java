package co.kr.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "Order")
public interface OrderClient {

    @PatchMapping("/orders/{orderCode}/status")
    void updateOrderStatus(
            @PathVariable String orderCode,
            @RequestParam String status
    );
}
