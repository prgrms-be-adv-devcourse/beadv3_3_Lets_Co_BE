package co.kr.user.model.dto.login;

import lombok.Data;

/**
 * 사용자가 로그인을 성공했을 때, 이후 서비스 이용에 필요한 인증 정보를 담아 반환하는 DTO입니다.
 * JWT 기반의 액세스 토큰과 리프레시 토큰을 클라이언트에 제공합니다.
 */
@Data
public class LoginDTO {
    /** API 요청 시 Authorization 헤더에 사용될 액세스 토큰 */
    private String accessToken;
    /** 액세스 토큰 만료 시 재발급을 위해 사용되는 리프레시 토큰 */
    private String refreshToken;
}