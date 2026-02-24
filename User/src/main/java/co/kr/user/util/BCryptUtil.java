package co.kr.user.util;

import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 비밀번호 암호화 및 검증을 담당하는 유틸리티 클래스입니다.
 * Spring Security의 BCryptPasswordEncoder를 래핑하여 사용하기 쉽게 제공합니다.
 */
@Component
public class BCryptUtil {
    // BCrypt 해싱 알고리즘을 수행하는 인코더 객체 생성
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 평문 비밀번호를 BCrypt 알고리즘으로 해싱(암호화)합니다.
     * * @param password 사용자가 입력한 평문 비밀번호
     * @return 암호화된 해시 문자열 (Salt가 포함되어 매번 다른 결과가 나옴)
     */
    public String encode(String password) {
        return encoder.encode(password);
    }

    /**
     * 입력된 평문 비밀번호가 저장된 암호화된 비밀번호와 일치하는지 검증합니다.
     * * @param rawPassword 사용자가 입력한 평문 비밀번호
     * @param encodedPassword 데이터베이스에 저장된 암호화된 비밀번호
     * @return 일치하면 true, 그렇지 않으면 false
     */
    public boolean check(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}