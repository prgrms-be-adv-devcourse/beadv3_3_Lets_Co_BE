package co.kr.order.client;

import co.kr.order.model.dto.UserData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="User")
public interface UserClient {

    /*
     * @param userIdx
     * @param request : AddressInfo (주소 정보), CardInfo (카드 정보)
     */
    @PostMapping("/order/{userIdx}")
    UserData getUserData(
            @PathVariable Long userIdx,
            @RequestBody UserData request
    );

}
