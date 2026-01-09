package co.kr.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="User")
public interface UserClient {

    @GetMapping("/userIdx")
    Long getUserId(@RequestHeader("Authorization") String token);
}
