package co.kr.assistant.interceptor;

import co.kr.assistant.util.CookieUtil;
import co.kr.assistant.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${chat.rate-limit.per-minute:10}")
    private int maxRequestsPerMinute;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String chatToken = TokenUtil.getCookieValue(request, CookieUtil.CHAT_TOKEN_NAME);

        if (chatToken == null) {
            throw new IllegalArgumentException("채팅 세션이 존재하지 않습니다.");
        }

        try {
            String sessionData = (String) redisTemplate.opsForValue().get("session:" + chatToken);

            if (sessionData != null) {
                String currentIp = TokenUtil.getClientIp(request);
                String currentUa = request.getHeader("User-Agent");
                currentUa = (currentUa != null) ? currentUa : "UNKNOWN";

                String[] parts = sessionData.split("\\|");
                String originalIp = parts[0];
                String originalUa = parts.length > 1 ? parts[1] : "UNKNOWN";

                if (!originalUa.equals(currentUa)) {
                    redisTemplate.delete("session:" + chatToken);
                    throw new IllegalStateException("접속 환경이 변경되어 세션을 종료합니다.");
                }

                // [개선 2] Rolling Expiration: 활동 시마다 Redis 세션과 쿠키 만료 시간 갱신
                // Redis 만료 시간 연장 (1시간)
                redisTemplate.expire("session:" + chatToken, 1, TimeUnit.HOURS);

                // 브라우저 쿠키 만료 시간 연장 (1시간)
                CookieUtil.addCookie(response, CookieUtil.CHAT_TOKEN_NAME, chatToken, CookieUtil.CHAT_TOKEN_EXPIRY);

                if (!originalIp.equals(currentIp)) {
                    redisTemplate.opsForValue().set("session:" + chatToken, currentIp + "|" + currentUa, 1, TimeUnit.HOURS);
                }

                // Rate Limiting 로직
                String rateLimitKey = "rate_limit:" + chatToken;
                Long requestCount = redisTemplate.opsForValue().increment(rateLimitKey);
                if (requestCount != null && requestCount == 1) {
                    redisTemplate.expire(rateLimitKey, 1, TimeUnit.MINUTES);
                }
                if (requestCount != null && requestCount > maxRequestsPerMinute) {
                    throw new IllegalStateException("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
                }
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis 장애 감지 - 가용성 확보를 위해 검증 생략: {}", e.getMessage());
        }

        return true;
    }
}