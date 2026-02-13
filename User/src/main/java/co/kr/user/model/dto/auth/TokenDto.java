package co.kr.user.model.dto.auth;

import lombok.Data;

@Data
public class TokenDto {
    private String accessToken;
    private String refreshToken;
}