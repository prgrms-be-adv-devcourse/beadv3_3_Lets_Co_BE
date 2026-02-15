package co.kr.user.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

/**
 * 인증 번호나 랜덤 코드를 생성하기 위한 유틸리티 클래스입니다.
 * 보안성이 높은 난수 생성기(SecureRandom)를 사용합니다.
 */
@Component
public class RandomCodeUtil {
    // 코드 생성에 사용할 문자셋 (영문 대소문자 + 숫자, 혼동하기 쉬운 0, 1, I, O 등은 제외하지 않고 포함됨)
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    // 암호학적으로 강력한 난수 생성기 인스턴스
    private static final SecureRandom RANDOM = new SecureRandom();
    // 생성될 코드의 최소 길이
    private static final int MIN_LENGTH = 10;
    // 최소 길이에 더해질 수 있는 최대 추가 길이 (즉, 전체 길이는 10 ~ 29 사이)
    private static final int MAX_ADDITIONAL_LENGTH = 20;

    /**
     * 랜덤한 길이와 구성을 가진 코드를 생성하여 반환합니다.
     * @return 생성된 랜덤 문자열
     */
    public String getCode() {
        // 10에서 29 사이의 랜덤한 길이 결정
        int length = MIN_LENGTH + RANDOM.nextInt(MAX_ADDITIONAL_LENGTH);

        // 문자열 조작 효율성을 위해 StringBuilder 사용
        StringBuilder code = new StringBuilder(length);

        // 결정된 길이만큼 반복하며 랜덤 문자 추가
        for (int i = 0; i < length; i++) {
            // CHARACTERS 문자열에서 랜덤한 인덱스의 문자를 선택하여 추가
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }

        return code.toString();
    }
}