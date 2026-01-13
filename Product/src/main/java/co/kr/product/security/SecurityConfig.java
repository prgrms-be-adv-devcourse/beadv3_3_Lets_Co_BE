package co.kr.product.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

// 테스트용 임시 설정
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1.  CSRF 보호 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 모든 요청에 대해 로그인(인증) 요구
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )

                // 3. Basic Auth 활성화 (Postman 사용 시 필수)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}