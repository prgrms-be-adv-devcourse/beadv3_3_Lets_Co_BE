package co.kr.order.client;

import co.kr.order.model.dto.UserData;
import co.kr.order.model.dto.request.UserDataRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name="User")
public interface UserClient {
    /*
     * @param userIdx
     * @param request : AddressInfo (주소 정보), CardInfo (카드 정보)
     */
    @PostMapping("/users/order/{userIdx}")
    UserData getUserData(
            @PathVariable Long userIdx,
            @RequestBody UserDataRequest request
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
}