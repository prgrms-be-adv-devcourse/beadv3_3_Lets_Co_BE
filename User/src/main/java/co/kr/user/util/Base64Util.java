package co.kr.user.util;

import org.springframework.stereotype.Component;
import java.util.Base64;

/**
 * Base64 인코딩 및 디코딩 기능을 제공하는 유틸리티 클래스입니다.
 * 바이너리 데이터나 문자열을 텍스트 형식으로 안전하게 전송하거나 저장할 때 사용됩니다.
 */
@Component
public class Base64Util {

    /**
     * 일반 문자열을 Base64 형식의 문자열로 인코딩(변환)합니다.
     *
     * @param data 인코딩할 원본 문자열
     * @return Base64로 인코딩된 문자열
     */
    public String encode(String data) {
        // 문자열을 바이트 배열로 변환 후 Base64 인코딩 수행
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    /**
     * Base64 형식으로 인코딩된 문자열을 다시 원본 문자열로 디코딩(복원)합니다.
     *
     * @param data Base64로 인코딩된 문자열
     * @return 복호화된 원본 문자열
     */
    public String decode(String data) {
        // Base64 문자열을 바이트 배열로 디코딩
        byte[] decodedBytes = Base64.getDecoder().decode(data);

        // 바이트 배열을 다시 문자열로 변환하여 반환
        return new String(decodedBytes);
    }
}