package co.kr.user.util;

import org.springframework.stereotype.Component;
import java.util.Base64;

@Component
public class Base64Util {

    public String encode(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    public String decode(String data) {
        byte[] decodedBytes = Base64.getDecoder().decode(data);

        return new String(decodedBytes);
    }
}