package co.kr.user.service;

import co.kr.user.model.dto.login.LoginDTO;
import co.kr.user.model.dto.login.LoginReq;

/**
 * 일반 로그인 및 로그아웃 처리를 위한 서비스 인터페이스입니다.
 * 아이디/비밀번호 기반의 인증과 토큰 발급 로직을 정의합니다.
 */
public interface LoginService {

    /**
     * 사용자의 아이디와 비밀번호를 검증하고, 유효한 경우 JWT 토큰(Access/Refresh)을 발급합니다.
     * @param loginReq 로그인 요청 정보 (아이디, 비밀번호)
     * @return 발급된 액세스 토큰과 리프레시 토큰이 담긴 DTO
     */
    LoginDTO login(LoginReq loginReq);

    /**
     * 로그아웃 처리를 수행합니다.
     * 리프레시 토큰을 무효화하거나 블랙리스트에 추가하여 더 이상 사용할 수 없게 만듭니다.
     * @param refreshToken 로그아웃할 사용자의 리프레시 토큰
     * @return 처리 결과 메시지 ("로그아웃 되었습니다.")
     */
    String logout(String refreshToken);
}