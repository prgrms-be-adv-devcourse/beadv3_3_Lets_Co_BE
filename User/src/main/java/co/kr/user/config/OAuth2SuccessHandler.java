package co.kr.user.config;

import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.service.UserQueryService;
import co.kr.user.util.CookieUtil;
import co.kr.user.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OAuth2 소셜 로그인 성공 시 실행되는 핸들러 클래스입니다.
 * 로그인 성공 후 JWT 토큰을 생성하고, 쿠키에 저장하며, 클라이언트 페이지로 리다이렉트하는 역할을 합니다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserQueryService userQueryService;
    private final JWTUtil jwtUtil;

    // application.yml에서 설정된 Redis 저장 키 접두사 (예: "RT:")
    @Value("${custom.security.redis.rt-prefix}")
    private String rtPrefix;

    // 리다이렉트할 기본 사이트 URL
    @Value("${custom.site.url.base}")
    private String baseUrl;

    /**
     * 인증이 성공했을 때 호출되는 메서드입니다.
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param authentication 인증 정보 객체 (OAuth2 사용자 정보 포함)
     * @throws IOException 입출력 예외 발생 시
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication
    ) throws IOException {
        // 인증된 사용자 정보(Principal)를 OAuth2User로 캐스팅하여 가져옵니다.
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        // 로그인한 소셜 서비스의 등록 ID (google, kakao, naver 등)를 가져옵니다.
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        // 사용자 속성 맵(Map)을 가져옵니다.
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String loginId = "";

        // 소셜 서비스별로 상이한 ID 식별자를 추출합니다.
        if ("google".equals(registrationId)) {
            // 구글은 이메일을 ID로 사용
            loginId = (String) attributes.get("email");
        } else if ("kakao".equals(registrationId)) {
            // 카카오는 고유 ID 숫자를 사용
            loginId = attributes.get("id").toString();
        } else if ("naver".equals(registrationId)) {
            // 네이버는 response 객체 안에 id가 존재
            Map<String, Object> resp = (Map<String, Object>) attributes.get("response");
            loginId = (String) resp.get("id");
        }

        // 추출한 ID로 DB에서 활성 사용자 정보를 조회합니다.
        // (CustomOAuth2UserService에서 이미 가입 처리가 되었으므로 조회 가능해야 함)
        Users user = userQueryService.findActiveUserById(loginId);
        UsersInformation userInfo = userQueryService.findActiveUserInfo(user.getUsersIdx());

        // JWT Access Token 및 Refresh Token 생성
        String accessToken = jwtUtil.createAccessToken(user.getUsersIdx(), user.getCreatedAt(), user.getUpdatedAt());
        String refreshToken = jwtUtil.createRefreshToken(user.getUsersIdx());

        // Refresh Token을 Redis에 저장 (유효기간 7일)
        redisTemplate.opsForValue().set(rtPrefix + user.getUsersIdx(), refreshToken, 7, TimeUnit.DAYS);

        // 생성된 토큰들을 쿠키에 저장하여 클라이언트에 전달
        CookieUtil.addCookie(response, CookieUtil.ACCESS_TOKEN_NAME, accessToken, CookieUtil.ACCESS_TOKEN_EXPIRY);
        CookieUtil.addCookie(response, CookieUtil.REFRESH_TOKEN_NAME, refreshToken, CookieUtil.REFRESH_TOKEN_EXPIRY);

        // CustomOAuth2UserServiceImpl에서 설정한 초기값과 비교합니다.
        // 1. 기본 전화번호 그대로인지 확인
        // 2. 기본 생년월일 그대로인지 확인
        // 3. 이메일이 임시 도메인(@oauth.com)인지 확인
        boolean isProfileIncomplete = "010-0000-0000".equals(userInfo.getPhoneNumber())
                || "1900-01-01".equals(userInfo.getBirth())
                || (userInfo.getMail() != null && userInfo.getMail().endsWith("@oauth.com"));

        // 로그인 성공 후 리다이렉트 처리
        // 네이버나 카카오 로그인의 경우 추가 정보 입력 페이지 등으로 이동시킬 수 있음 (비즈니스 로직에 따라 다름)
        if (isProfileIncomplete) {
            // 처음 로그인하거나 아직 정보를 수정하지 않은 경우
            response.sendRedirect(baseUrl + "/my/complete-profile");
        } else {
            // 이미 개인 정보를 입력한 경우
            response.sendRedirect(baseUrl + "/home");
        }
    }
}