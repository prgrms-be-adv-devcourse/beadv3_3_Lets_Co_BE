package co.kr.user.service;

import co.kr.user.model.DTO.login.LoginDTO;
import co.kr.user.model.DTO.login.LoginReq;

/**
 * 로그인(Login) 및 로그아웃(Logout) 관련 비즈니스 로직을 정의하는 인터페이스입니다.
 * 사용자 인증, 토큰 발급, 로그아웃 처리 기능을 명세합니다.
 * 구현체: LoginService
 */
public interface LoginServiceImpl {

    /**
     * 로그인 처리 메서드 정의입니다.
     * 사용자 인증(아이디/비밀번호 검증)을 수행하고, 성공 시 JWT 토큰(Access/Refresh)을 발급합니다.
     *
     * @param loginReq 로그인 요청 정보 (아이디, 비밀번호)
     * @return LoginDTO 발급된 토큰 정보 (AccessToken, RefreshToken)
     */
    LoginDTO login(LoginReq loginReq);

    /**
     * 로그아웃 처리 메서드 정의입니다.
     * 리프레시 토큰을 만료 처리하여 더 이상 토큰 갱신이 불가능하도록 합니다.
     *
     * @param refreshToken 로그아웃할 사용자의 리프레시 토큰
     * @return 로그아웃 처리 결과 메시지
     */
    String logout(String refreshToken);
}