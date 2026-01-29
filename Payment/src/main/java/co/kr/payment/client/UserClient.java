package co.kr.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "User")
public interface UserClient {
        
//      기존 PaymentServiceImpl 에서 작성했던 주석을 기반으로 엔드포인트 작성함_cjm
//      이후 User 에 추가요청 해야함
//     @PostMapping("/users/{userIdx}/balance/pay")
//     void useBalance(
//             @PathVariable Long userIdx,
//             @RequestBody BigDecimal amount
//     );

//     @PostMapping("/users/{userIdx}/balance/refund")
//     void refundBalance(
//             @PathVariable Long userIdx,
//             @RequestBody BigDecimal amount
//     );
}
