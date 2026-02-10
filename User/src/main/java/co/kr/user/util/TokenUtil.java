package co.kr.user.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;

public class TokenUtil {
    private static final long THREE_DAYS_IN_SECONDS = 518400;

    public static boolean isTokenExpiringSoon(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return false;

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

            Integer exp = (Integer) claims.get("exp");
            if (exp == null) return false;

            long now = System.currentTimeMillis() / 1000;

            return (exp - now) < THREE_DAYS_IN_SECONDS;

        } catch (Exception e) {
            return false;
        }
    }
}