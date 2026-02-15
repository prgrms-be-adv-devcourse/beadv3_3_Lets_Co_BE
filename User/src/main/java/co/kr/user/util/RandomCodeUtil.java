package co.kr.user.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class RandomCodeUtil {
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MIN_LENGTH = 10;
    private static final int MAX_ADDITIONAL_LENGTH = 20;

    public String getCode() {
        int length = MIN_LENGTH + RANDOM.nextInt(MAX_ADDITIONAL_LENGTH);

        StringBuilder code = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }

        return code.toString();
    }
}