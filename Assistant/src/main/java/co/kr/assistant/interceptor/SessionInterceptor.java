package co.kr.assistant.interceptor;

import co.kr.assistant.util.CookieUtil;
import co.kr.assistant.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class SessionInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 쿠키에서 chatToken 추출
        String chatToken = TokenUtil.getCookieValue(request, CookieUtil.CHAT_TOKEN_NAME);

        if (chatToken == null) {
            throw new IllegalArgumentException("채팅 세션이 존재하지 않습니다.");
        }

        // 2. Redis에서 해당 토큰의 세션 정보 조회 (형식: "ip|userAgent")
        String sessionData = (String) redisTemplate.opsForValue().get("session:" + chatToken);

        if (sessionData == null) {
            throw new IllegalArgumentException("세션이 만료되었습니다. 다시 시작해주세요.");
        }

        // 3. 현재 요청의 IP와 User-Agent 추출
        String currentIp = TokenUtil.getClientIp(request);
        String currentUa = request.getHeader("User-Agent");

        // 4. 저장된 정보와 대조 (기기/장소 변경 감지)
        String[] parts = sessionData.split("\\|");
        String originalIp = parts[0];
        String originalUa = parts[1];

        if (!originalIp.equals(currentIp) || !originalUa.equals(currentUa)) {
            // 정보가 다르면 토큰이 탈취된 것으로 간지하고 세션 즉시 삭제 (보안 강화)
            redisTemplate.delete("session:" + chatToken);
            throw new IllegalStateException("비정상적인 접근이 감지되어 세션을 종료합니다.");
        }

        return true; // 검증 통과
    }
}