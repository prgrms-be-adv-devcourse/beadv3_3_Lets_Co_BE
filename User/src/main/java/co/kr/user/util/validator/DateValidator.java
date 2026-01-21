package co.kr.user.util.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 커스텀 어노테이션 @ValiDate의 실제 검증 로직을 구현하는 클래스입니다.
 * 문자열로 입력된 날짜가 'YYYY-MM-DD' 형식인지, 그리고 오늘보다 과거의 날짜인지 확인합니다.
 */
public class DateValidator implements ConstraintValidator<ValiDate, String> {

    /**
     * 유효성 검증을 수행하는 메서드입니다.
     *
     * @param value 검증 대상이 되는 날짜 문자열 (예: "1990-01-01")
     * @param context 검증 컨텍스트 (오류 메시지 등을 설정할 때 사용 가능)
     * @return 유효하면 true, 유효하지 않으면 false
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null이거나 빈 문자열인 경우 유효한 것으로 간주합니다.
        // (필수 입력값 검증은 @NotNull, @NotEmpty 등을 별도로 사용해야 함을 의미합니다.)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        try {
            // 입력된 문자열을 ISO_DATE 형식(YYYY-MM-DD)으로 파싱을 시도합니다.
            // 형식이 맞지 않으면 DateTimeParseException 예외가 발생하여 catch 블록으로 이동합니다.
            LocalDate birthDate = LocalDate.parse(value, DateTimeFormatter.ISO_DATE);

            // 파싱된 날짜가 현재 날짜(LocalDate.now())보다 이전인지 확인합니다.
            // 생년월일은 미래일 수 없으므로 과거여야 true를 반환합니다.
            return birthDate.isBefore(LocalDate.now());

        } catch (DateTimeParseException e) {
            // 날짜 형식이 올바르지 않은 경우(예: "2023/01/01", "invalid-date") 유효하지 않음으로 처리합니다.
            return false;
        }
    }
}