package co.kr.order.client;

import co.kr.order.model.dto.UserData;
import co.kr.order.model.dto.request.UserDataRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="User")
public interface UserClient {

    // 이거 삭제합니다. (header로 userIdx 를 주기 때문에)
    @GetMapping("/userIdx")
    Long getUserIdx(@RequestHeader("Authorization") String token);

    /*
     * @param userIdx
     * @param request : AddressInfo (주소 정보), CardInfo (카드 정보)
     */
    @PostMapping("/order")
    UserData getUserData(
            Long userIdx,
            @RequestBody UserDataRequest request
    );
}
