package co.kr.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "Order")
public interface OrderClient {

    @PostMapping("/orders/{orderCode}/status")
    void updateOrderStatus(
            @PathVariable String orderCode,
            @RequestParam String status
    );

    // 수동 테스트용: ordersIdx 없이 confirm 호출 시 Order 서비스에서 조회하기 위한 메서드
    // 실제 운영 흐름에서는 Order 서비스가 ordersIdx를 포함하여 Payment를 호출하므로 사용되지 않음
    @GetMapping("/orders/{orderCode}")
    Map<String, Object> getOrder(
            @PathVariable String orderCode,
            @RequestHeader("X-USERS-IDX") Long userIdx
    );
}
