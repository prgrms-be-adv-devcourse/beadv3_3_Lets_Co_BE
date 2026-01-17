package co.kr.user.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.util.Base64;

@Slf4j
public class TokenUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final long REISSUE_LIMIT = 6L * 24 * 60 * 60 * 1000; // 6일

    /**
     * 토큰 만료 시간이 6일 이하로 남았는지 확인
     */
    public static boolean isTokenExpiringSoon(String token) {
        if (token == null || token.isBlank()) return false;

        try {
            // 1. Payload 추출
            String[] chunks = token.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));

            // 2. JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(payload);
            long exp = jsonNode.get("exp").asLong();

            // 3. 남은 시간 계산
            long remainMs = (exp * 1000) - System.currentTimeMillis();

            log.debug("토큰 남은 시간: {} ms", remainMs);

            // 4. 6일 이하인지 체크
            return remainMs > 0 && remainMs <= REISSUE_LIMIT;
        } catch (Exception e) {
            log.error("Token parsing error", e);
            return false;
        }
    }
}