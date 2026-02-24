package co.kr.user.util;

import java.util.Base64;

/**
 * Base64 인코딩 및 디코딩을 위한 유틸리티 클래스입니다.
 * Java 표준 라이브러리의 Base64 기능을 래핑하여 제공합니다.
 */
public class Base64Util {

    /**
     * 유틸리티 클래스이므로 인스턴스 생성을 방지하기 위해 생성자를 private으로 선언합니다.
     * 생성 시도 시 예외를 발생시킵니다.
     */
    private Base64Util() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 문자열을 Base64 포맷으로 인코딩합니다.
     * @param data 인코딩할 원본 문자열
     * @return Base64로 인코딩된 문자열
     */
    public static String encode(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    /**
     * Base64 포맷의 문자열을 디코딩합니다.
     * @param data 디코딩할 Base64 문자열
     * @return 디코딩된 원본 문자열
     */
    public static String decode(String data) {
        return new String(Base64.getDecoder().decode(data));
    }
}