package co.kr.payment.client;

import co.kr.payment.model.dto.request.BalanceClientReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", path = "/client/users" , url = "http://user-service:8080")
public interface UserClient {

    @PostMapping("/balance")
    void updateBalance(
            @RequestBody BalanceClientReq request
    );
}
