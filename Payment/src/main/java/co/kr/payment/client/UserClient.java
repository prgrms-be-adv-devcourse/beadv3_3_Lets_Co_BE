package co.kr.payment.client;

import co.kr.payment.model.dto.request.BalanceClientReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", path = "/client/users" , url = "http://user-service:8080")
public interface UserClient {

    @PostMapping("/{userIdx}/balance")
    void updateBalance(
            @PathVariable("userIdx") Long userIdx,
            @RequestBody BalanceClientReq request
    );
}
