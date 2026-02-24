package co.kr.product.common.auth;

import co.kr.product.product.client.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthAdapter {
    private final AuthServiceClient authServiceClient;

    public String getUserRole(Long userIdx){
        String body = authServiceClient.getUserRole(userIdx).getBody();
        if (body != null) {
            return body.replaceAll("\"", ""); // 큰따옴표 제거
        }
        return null;
    }
}
