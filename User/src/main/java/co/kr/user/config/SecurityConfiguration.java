package co.kr.user.config;

import co.kr.user.service.Impl.CustomOAuth2UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// [Security Note]
// 이 서비스는 Gateway 뒤에 위치하며, Gateway가 1차적으로 JWT 검증을 완료했다고 가정합니다.
// 따라서 이 코드는 'X-USERS-IDX' 헤더를 신뢰합니다.
// 배포 시, 반드시 Gateway IP에서 오는 요청만 허용하도록 방화벽(Security Group)을 설정해야 합니다.
/**
 * Spring Security 설정 클래스입니다.
 * 애플리케이션의 보안 정책(인증, 인가, 세션 관리 등)을 정의합니다.
 */
@Configuration
public class SecurityConfiguration {

    /**
     * HTTP 보안 필터 체인을 구성하여 빈으로 등록합니다.
     * @param http HttpSecurity 객체
     * @param oAuth2SuccessHandler 로그인 성공 핸들러
     * @param customOAuth2UserServiceImpl OAuth2 사용자 정보 서비스
     * @return 구성된 SecurityFilterChain
     * @throws Exception 설정 중 예외 발생 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   OAuth2SuccessHandler oAuth2SuccessHandler,
                                                   CustomOAuth2UserServiceImpl customOAuth2UserServiceImpl
    ) throws Exception {

        return http
                // CSRF(Cross-Site Request Forgery) 보호 비활성화 (REST API 서버이므로 세션 기반 CSRF 토큰이 필요 없음)
                .csrf(AbstractHttpConfigurer::disable)
                // CORS(Cross-Origin Resource Sharing) 설정 비활성화 (필요에 따라 별도 설정 가능)
                .cors(AbstractHttpConfigurer::disable)
                // HTTP Basic 인증 비활성화 (JWT를 사용하므로 불필요)
                .httpBasic(AbstractHttpConfigurer::disable)
                // 폼 로그인 비활성화 (API 서버이므로 자체 로그인 페이지 미사용)
                .formLogin(AbstractHttpConfigurer::disable)
                // 세션 관리 정책 설정: STATELESS (서버에 세션을 생성하지 않음, JWT 사용)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 모든 요청에 대해 인증 없이 접근 허용 (Gateway 등 앞단에서 제어하거나 추후 상세 설정 필요)
                        .anyRequest().permitAll()
                )
                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        // 리다이렉션 엔드포인트 설정 (소셜 로그인 후 돌아올 URL 패턴)
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/*")
                        )
                        // 사용자 정보 엔드포인트 설정 (로그인 성공 후 사용자 정보를 가져올 서비스 등록)
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserServiceImpl))
                        // 로그인 성공 시 실행할 핸들러 등록
                        .successHandler(oAuth2SuccessHandler)
                )
                .build();
    }
}