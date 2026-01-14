package co.kr.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * [스프링 시큐리티 설정 클래스]
 * 애플리케이션의 보안 정책을 정의하는 곳입니다.
 * - 인증(Authentication): 누구인가? (로그인)
 * - 인가(Authorization): 권한이 있는가? (접근 제어)
 * * 최근 스프링 부트 버전에서는 WebSecurityConfigurerAdapter를 상속받지 않고,
 * SecurityFilterChain을 Bean으로 등록하는 방식을 권장합니다.
 */
@Configuration // 스프링 설정 클래스임을 명시 (Bean 등록 가능)
public class SecurityConfiguration {

    /**
     * [보안 필터 체인 정의]
     * HttpSecurity 객체를 통해 구체적인 보안 규칙을 설정합니다.
     * * @param http HttpSecurity 설정 객체
     * @return SecurityFilterChain (설정된 보안 필터들의 모음)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 1. [CSRF 비활성화]
                // CSRF(사이트 간 요청 위조) 보호 기능은 주로 브라우저의 쿠키/세션을 이용한 공격을 막기 위해 사용됩니다.
                // REST API 서버는 보통 세션을 사용하지 않고 토큰(JWT 등) 방식을 쓰거나,
                // Stateless하게 동작하므로 CSRF 보호를 끕니다.
                .csrf(AbstractHttpConfigurer::disable)

                // 2. [CORS 설정] (현재 비활성화)
                // 다른 도메인(예: React 프론트엔드)에서 API를 호출할 때 발생하는 CORS 문제를 해결하려면 활성화해야 합니다.
                // 현재는 disable 상태이므로, 필요 시 .cors(Customizer.withDefaults()) 등으로 설정을 추가해야 합니다.
                .cors(AbstractHttpConfigurer::disable)

                // 3. [기본 인증 방식 비활성화]
                // httpBasic: 헤더에 ID/PW를 실어 보내는 브라우저 팝업 방식 -> 사용 안 함
                .httpBasic(AbstractHttpConfigurer::disable)

                // 4. [폼 로그인 비활성화]
                // formLogin: 스프링 시큐리티가 제공하는 기본 로그인 페이지 및 처리 로직 -> 사용 안 함
                // (API 서버이므로 JSON으로 데이터를 주고받는 커스텀 로그인 방식을 사용할 예정)
                .formLogin(AbstractHttpConfigurer::disable)

                // 5. [세션 정책 설정] (중요!)
                // SessionCreationPolicy.STATELESS: 서버에 세션을 생성하거나 저장하지 않습니다.
                // 즉, 요청이 올 때마다 인증 정보를 새로 확인합니다. (JWT 인증 등에 필수 설정)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 6. [URL별 접근 권한 설정]
                // requestMatchers: 특정 경로에 대한 규칙을 지정합니다.
                // permitAll(): 인증 없이 누구나 접근 가능
                // authenticated(): 인증(로그인)된 사용자만 접근 가능
                .authorizeHttpRequests(auth -> auth
                        // [로그인 및 인증 관련]
                        // POST /login 요청은 누구나 가능해야 함
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()

                        // /auth/** 경로(회원가입, 이메일 체크 등)는 누구나 접근 가능
                        .requestMatchers("/auth/**").permitAll()

                        // [계좌 관련]
                        // POST /accounts/** 요청도 현재는 열어둠
                        .requestMatchers(HttpMethod.POST, "/accounts/**").permitAll()

                        // [그 외 모든 요청]
                        // 개발 편의를 위해 일단 모든 요청을 허용(.permitAll()) 해둔 상태입니다.
                        // 실제 배포 시에는 .anyRequest().authenticated()로 변경하여
                        // 위에서 명시하지 않은 URL은 로그인한 사람만 쓰도록 막아야 보안상 안전합니다.
                        .anyRequest().permitAll()
                )
                .build(); // 설정 완료 및 필터 체인 생성
    }
}