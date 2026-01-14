package co.kr.user.util.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * [날짜 검증 로직 구현체]
 * @ValiDate 어노테이션이 붙은 필드의 값이 유효한지 실제로 검사하는 클래스입니다.
 * ConstraintValidator<ValiDate, String>를 구현하여,
 * 'ValiDate' 어노테이션이 붙은 'String' 타입의 필드를 검증함을 명시합니다.
 */
public class DateValidator implements ConstraintValidator<ValiDate, String> {

    /**
     * [검증 메서드]
     * @param value 검증할 대상 값 (여기서는 사용자가 입력한 생년월일 문자열, 예: "2024-05-01")
     * @param context 검증 컨텍스트 (에러 메시지 커스터마이징 등에 사용되나 여기서는 기본값 사용)
     * @return true(유효함), false(유효하지 않음)
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 1. [Null 체크 패스]
        // 값이 null이거나 빈 문자열인 경우, 유효한 것으로 간주하고 true를 반환합니다.
        // 이유: 필수 값 체크는 @NotBlank 같은 다른 어노테이션이 담당하는 것이 역할 분리에 좋기 때문입니다.
        // 즉, 값이 '있다면' 형식이 맞는지 검사하겠다는 의미입니다.
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        try {
            // 2. [날짜 파싱 시도]
            // 입력된 문자열(String)을 ISO 날짜 형식(YYYY-MM-DD)에 맞춰 LocalDate 객체로 변환합니다.
            // 이 과정에서 "2024-13-01"이나 "2024-02-30" 같이 달력에 없는 날짜가 들어오면
            // DateTimeParseException 예외가 발생하여 catch 블록으로 이동합니다.
            LocalDate birthDate = LocalDate.parse(value, DateTimeFormatter.ISO_DATE);

            // 3. [미래 날짜 검증]
            // 생년월일이 오늘 날짜보다 이후(미래)라면 유효하지 않은 데이터입니다.
            // isBefore(LocalDate.now()) -> 오늘보다 이전 날짜여야 true 반환
            return birthDate.isBefore(LocalDate.now());

        } catch (DateTimeParseException e) {
            // 4. [예외 처리]
            // 날짜 형식이 깨졌거나("abcd", "1990/01/01"), 존재하지 않는 날짜인 경우
            // 검증 실패(false)로 처리합니다.
            return false;
        }
    }
}