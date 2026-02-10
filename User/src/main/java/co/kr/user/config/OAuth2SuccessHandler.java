package co.kr.user.config;

import co.kr.user.dao.UserRepository;
import co.kr.user.model.entity.Users;
import co.kr.user.util.CookieUtil;
import co.kr.user.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication
    ) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String loginId = "";

        if ("google".equals(registrationId)) {
            loginId = (String) attributes.get("email");
        } else if ("kakao".equals(registrationId)) {
            loginId = attributes.get("id").toString();
        } else if ("naver".equals(registrationId)) {
            Map<String, Object> resp = (Map<String, Object>) attributes.get("response");
            loginId = (String) resp.get("id");
        }

        Users user = userRepository.findByIdAndDel(loginId, 0).orElseThrow();

        String accessToken = jwtUtil.createAccessToken(user.getUsersIdx(), user.getCreatedAt(), user.getUpdatedAt());
        String refreshToken = jwtUtil.createRefreshToken(user.getUsersIdx());

        redisTemplate.opsForValue().set("RT:" + user.getUsersIdx(), refreshToken, 7, TimeUnit.DAYS);
        CookieUtil.addCookie(response, "accessToken", accessToken, 15 * 60);
        CookieUtil.addCookie(response, "refreshToken", refreshToken, 7 * 24 * 60 * 60);

        if ("naver".equals(registrationId) || "kakao".equals(registrationId)) {
            response.sendRedirect("http://localhost:3000/my/complete-profile");
        } else {
            response.sendRedirect("http://localhost:3000/home");
        }
    }
}