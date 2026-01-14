package co.kr.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화
                .cors(AbstractHttpConfigurer::disable) // CORS 설정 (필요시 활성화)
                .httpBasic(AbstractHttpConfigurer::disable) // Basic Auth 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // [변경] API 서버이므로 폼 로그인 비활성화
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // [중요] 구체적인 경로를 먼저 선언
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers("/auth/**").permitAll() // GET, POST 등 모든 메서드 허용
                        .requestMatchers(HttpMethod.POST, "/accounts/**").permitAll()
                        // 개발 중 테스트를 위해 잠시 모두 허용 (배포 시 수정 필요)
                        .anyRequest().permitAll()
                )
                .build();
    }
}