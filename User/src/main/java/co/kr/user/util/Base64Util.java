package co.kr.user.util;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * The Base64Util class provides utility methods for encoding and decoding strings to and from Base64 format.
 * This utility is useful for handling data that needs to be encoded for safe transmission or storage.
 */
@Component
public class Base64Util {

    /**
     * Encodes a given string into Base64 format.
     *
     * This method is useful for encoding sensitive information (e.g., passwords or tokens)
     * to ensure safe transmission or storage.
     *
     * @param data The plain text string to encode.
     * @return The encoded string in Base64 format.
     */
    public String encode(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    /**
     * Decodes a given Base64 encoded string back to its original format.
     *
     * This method is useful for reversing the encoding process when retrieving sensitive information.
     *
     * @param data The Base64 encoded string to decode.
     * @return The decoded string in its original format.
     */
    public String decode(String data) {
        byte[] decodedBytes = Base64.getDecoder().decode(data);
        return new String(decodedBytes);
    }
}