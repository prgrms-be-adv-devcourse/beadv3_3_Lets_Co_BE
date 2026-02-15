package co.kr.order.client;

import co.kr.order.model.dto.SellerInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

/*
 * FeignClient
 * User-Service에 API 요청
 */
@FeignClient(name="user-service", path = "/client/users", url = "http://user-service:8080")
public interface UserClient {

    /**
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * todo. 정민님 주석
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    @GetMapping("/seller")
    SellerInfo getSellerData(
            Set<Long> sellerIdxList
    );
}
