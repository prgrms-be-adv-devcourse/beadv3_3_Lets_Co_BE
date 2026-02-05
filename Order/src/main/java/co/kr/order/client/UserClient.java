package co.kr.order.client;

import co.kr.order.model.dto.SellerInfo;
import co.kr.order.model.dto.request.ClientUpdateBalanceReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@FeignClient(name="USER-SERSVICE", path = "/client/users")
public interface UserClient {

    @PostMapping("/balance/pay")
    void useBalance(
            @RequestBody ClientUpdateBalanceReq useBalanceRequest
    );

    @PostMapping("/balance/refund")
    void refundBalance(
            @RequestBody ClientUpdateBalanceReq refundBalanceRequest
    );

    @PostMapping("/settlement")
    void sendSettlementData(
            @RequestBody Map<Long, BigDecimal> settlementData
    );

    @GetMapping("/seller")
    SellerInfo getSellerData(
            Set<Long> sellerIdxList
    );
}
