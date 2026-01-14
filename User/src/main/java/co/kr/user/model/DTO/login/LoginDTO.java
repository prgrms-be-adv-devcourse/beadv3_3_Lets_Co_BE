package co.kr.user.model.DTO.login;

import lombok.Data;

@Data
public class LoginDTO {

    private String accessToken;
    private String refreshToken;
}
