package co.kr.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정 클래스입니다.
 * 웹 애플리케이션의 보안 필터 체인을 구성하며, 인증(Authentication) 및 인가(Authorization) 정책을 정의합니다.
 */
@Configuration // 스프링 설정 클래스임을 명시 (Bean 등록 가능)
public class SecurityConfiguration {

    /**
     * SecurityFilterChain을 빈으로 등록하여 HTTP 보안 설정을 정의합니다.
     * CSRF, CORS, 세션 정책, 로그인 방식 등을 설정합니다.
     *
     * @param http HttpSecurity 객체 (보안 설정을 위한 빌더)
     * @return 구성된 SecurityFilterChain 객체
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF(Cross-Site Request Forgery) 보호 기능을 비활성화합니다.
                // REST API 서버는 세션을 사용하지 않고(Stateless) 토큰 방식을 주로 사용하므로 CSRF 보호가 불필요한 경우가 많습니다.
                .csrf(AbstractHttpConfigurer::disable)

                // Spring Security 차원의 CORS(Cross-Origin Resource Sharing) 설정을 비활성화합니다.
                // (필요 시 WebMvcConfig 등에서 전역 CORS 설정을 하거나, 여기서 CorsConfigurationSource를 등록해야 합니다.)
                .cors(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 방식을 비활성화합니다.
                // (요청 헤더에 ID/PW를 실어 보내는 브라우저 기반의 기본 팝업 인증창을 사용하지 않음)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 폼 로그인 방식을 비활성화합니다.
                // (Spring Security가 기본으로 제공하는 로그인 페이지 및 리다이렉션을 사용하지 않음)
                .formLogin(AbstractHttpConfigurer::disable)

                // 세션 관리 정책을 설정합니다.
                .sessionManagement(session ->
                        // 세션 생성 정책을 STATELESS(무상태)로 설정합니다.
                        // 서버가 세션을 생성하거나 유지하지 않으며, 클라이언트의 요청마다 인증 정보(JWT 등)를 확인합니다.
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // HTTP 요청에 대한 접근 권한(인가)을 설정합니다.
                .authorizeHttpRequests(auth -> auth
                        // 현재 설정: 모든 요청(.anyRequest())에 대해 인증 없이 접근을 허용(.permitAll())합니다.
                        // 추후 인증이 필요한 경로는 .authenticated() 또는 .hasRole() 등으로 변경해야 합니다.
                        .anyRequest().permitAll()
                )

                // 설정된 보안 필터 체인을 빌드하여 반환합니다.
                .build();
    }
}