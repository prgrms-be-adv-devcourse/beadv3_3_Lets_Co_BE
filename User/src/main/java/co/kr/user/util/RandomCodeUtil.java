package co.kr.user.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

/**
 * 보안성이 강화된 무작위 코드 생성 유틸리티 클래스입니다.
 * 영문 대소문자와 숫자를 조합하여 예측 불가능한 랜덤 문자열을 생성합니다.
 * 단순 java.util.Random 대신 암호학적으로 안전한 SecureRandom을 사용합니다.
 */
@Component
public class RandomCodeUtil {
    // 코드 생성에 사용할 문자 집합 (I, l, 1, O, 0 등 혼동하기 쉬운 문자는 일부 제외된 것으로 보임)
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    // 암호학적으로 강력한 난수 생성기
    private static final SecureRandom RANDOM = new SecureRandom();

    // 코드의 최소 길이
    private static final int MIN_LENGTH = 10;

    // 추가될 수 있는 최대 길이 (즉, 총 길이는 MIN_LENGTH ~ MIN_LENGTH + MAX_ADDITIONAL_LENGTH - 1)
    private static final int MAX_ADDITIONAL_LENGTH = 30;

    /**
     * 설정된 범위 내의 랜덤한 길이를 가진 무작위 문자열 코드를 생성합니다.
     * 10자에서 최대 39자 사이의 길이로 생성됩니다.
     *
     * @return 생성된 랜덤 코드 문자열
     */
    public String getCode() {
        // 10 이상 40 미만의 랜덤한 길이 결정
        int length = MIN_LENGTH + RANDOM.nextInt(MAX_ADDITIONAL_LENGTH);

        StringBuilder code = new StringBuilder(length);

        // 결정된 길이만큼 루프를 돌며 랜덤 문자 선택 및 추가
        for (int i = 0; i < length; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }

        return code.toString();
    }
}