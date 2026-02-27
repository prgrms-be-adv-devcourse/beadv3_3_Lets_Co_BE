package co.kr.customerservice.client;

import co.kr.customerservice.client.dto.ClientRoleDTO;
import co.kr.customerservice.common.model.dto.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// @FeignClient(name = "auth-service", url = "http://user-service:8080")

@FeignClient(name = "user-service", url = "http://user-service:8080")
public interface AuthServiceClient {
    @GetMapping("/client/users/role")
    ResponseEntity<BaseResponse<ClientRoleDTO>> getUserRole(@RequestParam("userIdx") Long userIdx);
}