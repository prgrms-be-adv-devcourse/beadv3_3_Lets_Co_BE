package co.kr.user.util;

import org.springframework.stereotype.Component;
import java.util.Base64;

/**
 * [Base64 유틸리티 클래스]
 * 문자열 데이터를 Base64 포맷으로 변환(Encoding)하거나,
 * Base64로 변환된 데이터를 다시 원본 문자열로 복구(Decoding)하는 기능을 제공합니다.
 * * * Base64란?
 * - 이진 데이터(Binary Data)를 ASCII 문자(텍스트)로 바꾸는 인코딩 방식입니다.
 * - 주로 통신 과정에서 특수문자 처리를 안전하게 하거나, 암호화 전 데이터를 표준화할 때 사용됩니다.
 */
@Component // 스프링 빈(Bean)으로 등록하여 다른 클래스에서 의존성 주입(DI) 받아 사용 가능
public class Base64Util {

    /**
     * [Base64 인코딩 메서드]
     * 평문(Normal String)을 입력받아 Base64 문자열로 변환합니다.
     * * * 사용 예시:
     * - "Hello" -> "SGVsbG8="
     * - 비밀번호 암호화 전단계(Pre-processing)에서 특수문자 문제를 피하기 위해 사용되기도 합니다.
     * * @param data 인코딩할 원본 문자열
     * @return Base64로 인코딩된 문자열
     */
    public String encode(String data) {
        // 1. data.getBytes(): 문자열을 바이트 배열(byte[])로 변환
        // 2. Base64.getEncoder().encodeToString(...): 바이트 배열을 Base64 문자열로 변환
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    /**
     * [Base64 디코딩 메서드]
     * Base64로 인코딩된 문자열을 입력받아 다시 원본 문자열로 복구합니다.
     * * * 사용 예시:
     * - "SGVsbG8=" -> "Hello"
     * - 클라이언트가 Base64로 인코딩해서 보낸 데이터를 서버에서 읽을 때 사용합니다.
     * * @param data Base64로 인코딩된 문자열
     * @return 복구된 원본 문자열
     */
    public String decode(String data) {
        // 1. Base64.getDecoder().decode(data): Base64 문자열을 바이트 배열로 복구
        byte[] decodedBytes = Base64.getDecoder().decode(data);

        // 2. new String(...): 바이트 배열을 다시 문자열 객체로 생성하여 반환
        return new String(decodedBytes);
    }
}