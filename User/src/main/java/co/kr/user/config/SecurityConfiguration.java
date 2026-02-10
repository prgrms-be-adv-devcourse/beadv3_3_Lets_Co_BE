package co.kr.user.config;

import co.kr.user.service.Impl.CustomOAuth2UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   OAuth2SuccessHandler oAuth2SuccessHandler,
                                                   CustomOAuth2UserServiceImpl customOAuth2UserServiceImpl
    ) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/*")
                        )
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserServiceImpl))
                        .successHandler(oAuth2SuccessHandler)
                )
                .build();
    }
}