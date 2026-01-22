package co.kr.costomerservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service", url = "http://user-service:8080")
public interface AuthServiceClient {
    @GetMapping("/auth/role")
    String getUserRole(@RequestParam("usersIdx") Long usersIdx);
}