package co.kr.payment.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "USER-SERVICE", path = "/client/users")
public interface UserClient {
        
//      기존 PaymentServiceImpl 에서 작성했던 주석을 기반으로 엔드포인트 작성함_cjm
//      이후 User 에 추가요청 해야함
//     @PostMapping("/{userIdx}/balance/pay")
//     void useBalance(
//             @PathVariable Long userIdx,
//             @RequestBody BigDecimal amount
//     );

//     @PostMapping("/{userIdx}/balance/refund")
//     void refundBalance(
//             @PathVariable Long userIdx,
//             @RequestBody BigDecimal amount
//     );
}
