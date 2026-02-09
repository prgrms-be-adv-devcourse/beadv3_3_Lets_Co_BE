package co.kr.user.model.dto.login;

import lombok.Data;

@Data
public class LoginDTO {
    private String accessToken;
    private String refreshToken;
}