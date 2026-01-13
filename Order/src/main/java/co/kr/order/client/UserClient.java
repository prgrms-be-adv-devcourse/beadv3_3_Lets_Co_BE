package co.kr.order.client;

import co.kr.order.model.dto.UserData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="User")
public interface UserClient {

    @GetMapping("/userIdx")
    Long getUserIdx(@RequestHeader("Authorization") String token);

    @GetMapping("/order")
    UserData getOrderData(@RequestHeader("Authorization") String token);
}
