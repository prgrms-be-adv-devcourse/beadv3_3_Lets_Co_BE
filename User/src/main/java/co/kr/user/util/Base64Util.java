package co.kr.user.util;

import java.util.Base64;

public class Base64Util {

    private Base64Util() {
        throw new IllegalStateException("Utility class");
    }

    public static String encode(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    public static String decode(String data) {
        return new String(Base64.getDecoder().decode(data));
    }
}