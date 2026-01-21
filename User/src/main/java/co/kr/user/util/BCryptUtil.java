package co.kr.user.util;

import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 해싱 알고리즘을 이용한 단방향 암호화 유틸리티 클래스입니다.
 * 비밀번호와 같이 원문을 복구할 필요가 없고, 보안성이 매우 높아야 하는 데이터를 저장할 때 사용합니다.
 * Salt를 자동으로 적용하여 같은 비밀번호라도 매번 다른 해시값이 생성되므로 Rainbow Table 공격에 안전합니다.
 */
@Component // 스프링 빈으로 등록
public class BCryptUtil {

    // Spring Security에서 제공하는 강력한 파수꾼, BCryptPasswordEncoder 인스턴스
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 평문(Raw Password)을 BCrypt 알고리즘으로 해싱(암호화)하는 메서드입니다.
     *
     * @param password 암호화할 평문 비밀번호
     * @return 해싱된 비밀번호 문자열 (String)
     */
    public String encode(String password) {
        // 평문 비밀번호를 받아 안전한 해시값으로 변환하여 반환
        return encoder.encode(password);
    }

    /**
     * 평문 비밀번호와 저장된 해시 비밀번호가 일치하는지 검증하는 메서드입니다.
     * 해시 함수는 단방향이므로, 입력된 평문을 같은 방식으로 해싱하여 저장된 값과 비교합니다.
     *
     * @param rawPassword 사용자가 입력한 평문 비밀번호
     * @param encodedPassword DB에 저장되어 있는 해싱된 비밀번호
     * @return 일치하면 true, 그렇지 않으면 false
     */
    public boolean check(String rawPassword, String encodedPassword) {
        // 입력된 비밀번호가 저장된 해시값과 매칭되는지 확인 (내부적으로 Salt 처리 등을 수행)
        return encoder.matches(rawPassword, encodedPassword);
    }
}