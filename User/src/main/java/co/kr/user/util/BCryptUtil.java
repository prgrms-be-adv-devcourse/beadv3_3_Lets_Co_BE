package co.kr.user.util;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import java.util.Base64;

/**
 * [비밀번호 암호화 유틸리티]
 * 사용자의 비밀번호를 안전하게 보호하기 위해 'Base64 인코딩'과 'BCrypt 해싱'을 조합하여 사용합니다.
 *
 * * BCrypt란?
 * - 비밀번호 저장을 위해 설계된 강력한 해시 함수입니다.
 * - 해시할 때마다 랜덤한 Salt(소금)를 자동으로 생성하여 포함하므로,
 * 같은 비밀번호라도 매번 다른 해시 결과(암호문)가 생성됩니다.
 * - 따라서 Rainbow Table 공격(미리 계산된 해시값 대조)에 강력합니다.
 */
@Component // 스프링 빈으로 등록 (어디서든 주입받아 사용 가능)
public class BCryptUtil {

    /**
     * [비밀번호 암호화 (설정) 메서드]
     * 회원가입이나 비밀번호 변경 시 사용됩니다.
     * 평문 비밀번호를 받아 암호화된 문자열로 변환하여 반환합니다.
     *
     * @param password 사용자가 입력한 원본 비밀번호 (예: "p@ssword123")
     * @return DB에 저장될 BCrypt 해시 문자열
     */
    public String encode(String password) {
        // 1. [Base64 인코딩]
        // 비밀번호를 바이트 배열로 변환한 뒤 Base64 문자열로 만듭니다.
        // 예: "1234" -> "MTIzNA=="
        // 이 과정은 비밀번호의 특수문자 등을 안전한 문자셋으로 변환하고,
        // 원본 값을 1차적으로 가리는(Obfuscation) 역할을 합니다.
        String basePW = Base64.getEncoder().encodeToString(password.getBytes());

        // 2. [BCrypt 해싱]
        // 인코딩된 문자열에 Salt를 섞어 강력하게 해싱합니다.
        // BCrypt.gensalt(): 랜덤 Salt 생성
        // 결과값 예시: $2a$10$rxxxx... (알고리즘 버전 + Cost + Salt + 해시값)
        return BCrypt.hashpw(basePW, BCrypt.gensalt());
    }

    /**
     * [비밀번호 검증 메서드]
     * 로그인 시 사용자가 입력한 비밀번호가 DB에 저장된 암호문과 일치하는지 확인합니다.
     * 주의: 해시 함수는 복호화가 불가능하므로, 입력된 값을 똑같이 해싱해서 비교해야 합니다.
     *
     * @param password 로그인 시 사용자가 입력한 평문 비밀번호
     * @param dbPassword DB에 저장되어 있는 암호화된 비밀번호 (해시값)
     * @return 일치하면 true, 다르면 false
     */
    public boolean check(String password, String dbPassword) {
        // 1. [동일한 전처리]
        // 비교를 위해 입력받은 비밀번호를 저장할 때와 똑같이 Base64로 인코딩합니다.
        String basePW = Base64.getEncoder().encodeToString(password.getBytes());

        // 2. [BCrypt 검증]
        // BCrypt.checkpw(평문, 해시값)
        // 이 메서드는 내부적으로 dbPassword(해시값)에서 Salt를 추출한 뒤,
        // basePW(입력값)를 그 Salt로 다시 해싱하여 결과가 같은지 비교합니다.
        return BCrypt.checkpw(basePW, dbPassword);
    }
}