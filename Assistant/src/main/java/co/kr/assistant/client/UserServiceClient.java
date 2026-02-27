package co.kr.assistant.client;

import co.kr.assistant.model.dto.user.UserContextDTO;
import co.kr.assistant.util.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// url은 실제 User 모듈이 실행되고 있는 주소와 포트로 맞춰주세요.
@FeignClient(name = "userService", url = "${user.service.url:http://localhost:8080}")
public interface UserServiceClient {

    // 수정됨: @PathVariable("userIdx")에서 ("userIdx") 부분 제거 (중복 선언 경고 해결)
    @GetMapping("/client/users/{userIdx}/context")
    BaseResponse<UserContextDTO> getUserContext(@PathVariable Long userIdx);
}