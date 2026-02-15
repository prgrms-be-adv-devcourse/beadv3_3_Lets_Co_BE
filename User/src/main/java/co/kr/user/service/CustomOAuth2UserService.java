package co.kr.user.service;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * OAuth2 소셜 로그인(구글, 카카오, 네이버 등) 시 사용자 정보를 로드하기 위한 서비스 인터페이스입니다.
 * Spring Security OAuth2의 DefaultOAuth2UserService를 확장하거나 대체하여 사용됩니다.
 */
public interface CustomOAuth2UserService {

    /**
     * OAuth2 공급자(Provider)로부터 액세스 토큰을 사용하여 사용자 정보를 가져옵니다.
     * 가져온 정보를 바탕으로 회원가입 또는 정보 업데이트를 수행하고, 인증된 사용자 객체(OAuth2User)를 반환합니다.
     * @param userRequest OAuth2 사용자 정보 요청 객체 (ClientRegistration, AccessToken 등 포함)
     * @return 인증된 OAuth2 사용자 객체 (Principal)
     * @throws OAuth2AuthenticationException 인증 실패 시 발생하는 예외
     */
    OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException;
}