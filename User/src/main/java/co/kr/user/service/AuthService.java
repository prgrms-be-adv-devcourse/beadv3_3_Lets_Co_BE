package co.kr.user.service;

import co.kr.user.model.dto.auth.TokenDto;

/**
 * 인증(Authentication) 및 토큰 관리를 위한 서비스 인터페이스입니다.
 * JWT 토큰의 재발급(Refresh) 로직을 정의합니다.
 */
public interface AuthService {

    /**
     * Refresh Token을 사용하여 Access Token을 재발급합니다.
     * Refresh Token의 만료가 임박한 경우(예: 3일 미만), Refresh Token도 함께 재발급(Rotation)합니다.
     * @param refreshToken 클라이언트로부터 전달받은 Refresh Token 문자열
     * @return 재발급된 Access Token과 (필요 시) Refresh Token이 담긴 DTO
     */
    TokenDto refreshToken(String refreshToken);
}