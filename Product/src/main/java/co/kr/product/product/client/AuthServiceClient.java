package co.kr.product.product.client;

import co.kr.product.common.BaseResponse;
import co.kr.product.product.client.dto.ClientRoleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// @FeignClient(name = "auth-service")
@FeignClient(name = "user-service", url = "http://user-service:8080")
public interface AuthServiceClient {
    @GetMapping("/client/users/role")
    ResponseEntity<BaseResponse<ClientRoleDTO>> getUserRole(@RequestParam("userIdx") Long userIdx);
}
