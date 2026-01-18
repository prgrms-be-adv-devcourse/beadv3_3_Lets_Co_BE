package co.kr.user.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;

public class TokenUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final long REISSUE_LIMIT = 6L * 24 * 60 * 60 * 1000;

    public static boolean isTokenExpiringSoon(String token) {
        if (token == null || token.isBlank()){
            return false;
        }

        try {
            String[] chunks = token.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));

            JsonNode jsonNode = objectMapper.readTree(payload);

            long exp = jsonNode.get("exp").asLong();
            long remainMs = (exp * 1000) - System.currentTimeMillis();

            return remainMs > 0 && remainMs <= REISSUE_LIMIT;

        } catch (Exception e) {
            return false;
        }
    }
}