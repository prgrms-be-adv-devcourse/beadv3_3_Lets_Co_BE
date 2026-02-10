package co.kr.payment.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", path = "/client/users" , url = "http://user-service:8080")
public interface UserClient {


}
