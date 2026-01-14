package co.kr.user.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

/**
 * [랜덤 코드 생성 유틸리티]
 * 이메일 인증 코드, 임시 비밀번호 등 예측 불가능한 문자열이 필요할 때 사용합니다.
 * 단순한 Random 클래스 대신 암호학적으로 더 안전한 SecureRandom을 사용합니다.
 */
@Component // 스프링 빈으로 등록하여 다른 서비스에서 주입받아 사용 가능
public class RandomCodeUtil {

    /**
     * [사용할 문자셋 정의]
     * 난수 생성 시 사용할 문자들을 정의합니다.
     * * 특징: 시각적으로 혼동하기 쉬운 문자들을 의도적으로 제거했습니다.
     * - 숫자: 0, 1 제외 (영문 O, I, l과 헷갈림 방지)
     * - 대문자: I, O 제외
     * - 소문자: l, o 제외
     * -> 이렇게 하면 사용자가 코드를 보고 입력할 때 실수를 줄일 수 있습니다.
     */
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    // [보안 난수 생성기]
    // 일반 Random 클래스는 시드값에 따라 예측이 가능할 수 있어 보안상 취약하므로,
    // 예측이 거의 불가능한 SecureRandom을 사용합니다.
    private static final SecureRandom RANDOM = new SecureRandom();

    // 생성할 코드의 최소 길이
    private static final int MIN_LENGTH = 10;

    // 추가될 수 있는 최대 길이 범위 (0 ~ 29)
    // 실제 길이는 MIN_LENGTH(10) + 0~29 = 10자 ~ 39자 사이가 됩니다.
    private static final int MAX_ADDITIONAL_LENGTH = 30;

    /**
     * [코드 생성 메서드]
     * 정의된 문자셋(CHARACTERS)에서 무작위로 문자를 뽑아 조합합니다.
     *
     * @return 생성된 랜덤 문자열
     */
    public String getCode() {
        // 1. [길이 결정]
        // 10 + (0부터 29 사이의 난수) = 10 ~ 39자 사이의 랜덤한 길이를 결정합니다.
        // (주석에는 8~15자로 되어 있으나, 실제 코드상 설정값은 더 길게 잡혀 있습니다.)
        int length = MIN_LENGTH + RANDOM.nextInt(MAX_ADDITIONAL_LENGTH);

        // 2. [문자열 조립 준비]
        // 빈번한 문자열 연산이 발생하므로 성능을 위해 StringBuilder를 사용합니다.
        StringBuilder code = new StringBuilder(length);

        // 3. [난수 추출 및 조립]
        // 결정된 길이만큼 반복문을 돌며 문자를 하나씩 뽑습니다.
        for (int i = 0; i < length; i++) {
            // CHARACTERS 문자열의 길이 범위 내에서 랜덤 인덱스를 하나 뽑고,
            // 그 위치에 있는 문자를 가져와서 결과 문자열에 이어 붙입니다.
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }

        // 4. [결과 반환]
        return code.toString();
    }
}