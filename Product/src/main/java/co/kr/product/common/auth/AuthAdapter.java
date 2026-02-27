package co.kr.product.common.auth;

import co.kr.product.product.client.AuthServiceClient;
import co.kr.product.product.client.dto.ClientRoleDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

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
