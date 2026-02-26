package co.kr.customerservice.common.auth;


import co.kr.customerservice.client.AuthServiceClient;
import co.kr.customerservice.client.dto.ClientRoleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthAdapter {
    private final AuthServiceClient authServiceClient;

    public ClientRoleDTO getUserData(Long userIdx){
        try {
            return authServiceClient.getUserRole(userIdx).getBody().data();

        }catch (Exception e){
            throw new IllegalArgumentException("유저 클라이언트 통신 실패 : " + e);
        }

    }
}
