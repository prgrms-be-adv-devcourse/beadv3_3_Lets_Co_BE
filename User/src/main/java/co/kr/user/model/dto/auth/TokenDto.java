package co.kr.user.model.dto.auth;

import lombok.Data;

/**
 * 사용자 인증 완료 또는 토큰 갱신 시 발급되는 JWT 토큰 정보를 전달하기 위한 DTO입니다.
 */
@Data
public class TokenDto {
    /**
     * API 요청 시 인증 헤더에 포함될 액세스 토큰(Access Token)입니다.
     * 주로 사용자 식별 정보와 만료 시간이 포함되어 있습니다.
     */
    private String accessToken;

    /**
     * 액세스 토큰 만료 시 새로운 토큰을 발급받기 위해 사용되는 리프레시 토큰(Refresh Token)입니다.
     * 보안을 위해 DB 또는 Redis에 저장되어 관리됩니다.
     */
    private String refreshToken;
}