package co.kr.product.product.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// @FeignClient(name = "auth-service")
@FeignClient(name = "user-service")
public interface AuthServiceClient {
    @GetMapping("/auth/role")
    String getUserRole(@RequestParam("usersIdx") Long usersIdx);
}
