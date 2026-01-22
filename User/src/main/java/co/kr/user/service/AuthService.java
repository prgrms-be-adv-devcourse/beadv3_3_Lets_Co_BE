package co.kr.user.service;

import co.kr.user.model.DTO.auth.TokenDto;
import co.kr.user.model.vo.UsersRole;

/**
 * 인증(Authentication) 및 권한(Authorization) 관련 공통 비즈니스 로직을 정의하는 인터페이스입니다.
 * 사용자 권한 조회와 리프레시 토큰을 이용한 토큰 재발급 기능을 명세합니다.
 * 구현체: AuthServiceImpl
 */
public interface AuthService {

    /**
     * 사용자 권한 조회 메서드 정의입니다.
     * 특정 사용자의 현재 권한(Role)을 확인합니다. (예: USER, SELLER, ADMIN)
     *
     * @param userIdx 사용자 고유 식별자
     * @return 사용자 권한 (UsersRole Enum)
     */
    UsersRole getRole(Long userIdx);

    /**
     * 토큰 재발급(Refresh) 메서드 정의입니다.
     * 만료된 Access Token을 대신하여, 유효한 Refresh Token으로 새로운 토큰 쌍을 발급받습니다.
     * Refresh Token Rotation(RTR) 정책이 적용될 수 있습니다.
     *
     * @param refreshToken 클라이언트로부터 전달받은 Refresh Token
     * @return TokenDto 새로 발급된 AccessToken 및 (선택적으로) RefreshToken
     */
    TokenDto refreshToken(String refreshToken);
}