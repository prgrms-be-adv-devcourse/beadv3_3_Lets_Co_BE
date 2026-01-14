package co.kr.order.client;

import co.kr.order.model.dto.UserData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="User")
public interface UserClient {

    /*
     * @param token : jwt Access 토큰 값
     * userIdx를 가져오기 User-Service 간의 동기통신
     */
    @GetMapping("/userIdx")
    Long getUserIdx(@RequestHeader("Authorization") String token);

    /*
     * @param token : jwt Access 토큰 값
     * UserData(userIdx, addressIdx, cardIdx)를 가져오기 User-Service 간의 동기통신
     */
    @GetMapping("/order")
    UserData getOrderData(@RequestHeader("Authorization") String token);
}
