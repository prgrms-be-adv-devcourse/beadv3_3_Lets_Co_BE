package co.kr.user.model.DTO.auth;

import lombok.Data;

@Data
public class TokenDto {
    private String accessToken;       // 무조건 갱신됨
    private String refreshToken;      // 갱신된 경우에만 값이 있음 (아니면 null)
}