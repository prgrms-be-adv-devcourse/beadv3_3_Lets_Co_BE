package co.kr.order.client;

import co.kr.order.model.dto.SellerInfo;
import co.kr.order.model.dto.UserData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

// @FeignClient(name="User")
@FeignClient(name = "user-service", url = "http://user-service:8080")
public interface UserClient {

    /*
     * @param userIdx
     * @param request : AddressInfo (주소 정보), CardInfo (카드 정보)
     */
    @PostMapping("/users/order/{userIdx}")
    UserData getUserData(
            @PathVariable Long userIdx,
            @RequestBody UserData request
    );

    /*
     * 예치금 결제 요청
     * @param userIdx 사용자 ID
     * @param amount 결제 금액
     */
    @PostMapping("/users/{userIdx}/balance/pay")
    void useBalance(
            @PathVariable Long userIdx,
            @RequestBody BigDecimal amount
    );

    /*
     * 예치금 환불
     * @param userIdx 사용자 ID
     * @param amount 환불 금액
     */
    @PostMapping("/users/{userIdx}/balance/refund")
    void refundBalance(
            @PathVariable Long userIdx,
            @RequestBody BigDecimal amount
    );

    @PostMapping("/users/settlement/")
    void sendSettlementData(@RequestBody Map<Long, BigDecimal> settlementData);

    @GetMapping("/users/seller/")
    SellerInfo getSellerData(Set<Long> sellerIdxList);
}
