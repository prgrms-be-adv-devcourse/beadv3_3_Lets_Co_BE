package co.kr.user.util.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 커스텀 어노테이션 @ValiDate의 유효성 검증 로직을 구현한 클래스입니다.
 * 생년월일 문자열의 형식, 미래 날짜 여부, 만 14세 미만 여부를 검증합니다.
 */
public class DateValidator implements ConstraintValidator<ValiDate, String> {

    /**
     * 실제 검증 로직을 수행하는 메서드입니다.
     * @param value 검증할 대상 값 (생년월일 문자열)
     * @param context 유효성 검증 컨텍스트 (에러 메시지 설정 등에 사용)
     * @return 유효하면 true, 유효하지 않으면 false
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null이거나 비어있는 값은 다른 어노테이션(@NotBlank 등)에서 처리하도록 여기서는 true 반환 (패스)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        try {
            // 입력된 문자열을 ISO_DATE 형식(YYYY-MM-DD)으로 파싱
            LocalDate birthDate = LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
            LocalDate today = LocalDate.now(); // 오늘 날짜

            // 1. 미래 날짜 검증: 생년월일이 오늘보다 미래인 경우 유효하지 않음
            if (birthDate.isAfter(today)) {
                buildCustomMessage(context, "생년월일은 미래 날짜일 수 없습니다.");
                return false;
            }

            // 2. 만 14세 미만 검증: 오늘 기준으로 14년 전 날짜 계산
            LocalDate minimumAgeDate = today.minusYears(14);

            // 생일이 만 14세 기준일보다 이후라면(더 어리면) 가입 불가
            if (birthDate.isAfter(minimumAgeDate)) {
                buildCustomMessage(context, "만 14세 미만은 가입할 수 없습니다.");
                return false;
            }

            // 모든 검증을 통과하면 유효함
            return true;
        } catch (DateTimeParseException e) {
            // 날짜 형식이 올바르지 않은 경우 (파싱 실패) -> @Pattern 등에서 이미 걸러지겠지만 안전을 위해 false 처리
            return false;
        }
    }

    /**
     * 기본 에러 메시지를 비활성화하고 커스텀 에러 메시지를 설정하는 헬퍼 메서드입니다.
     * @param context ConstraintValidatorContext
     * @param message 사용자에게 보여줄 에러 메시지
     */
    private void buildCustomMessage(ConstraintValidatorContext context, String message) {
        // 기본 메시지 템플릿 비활성화
        context.disableDefaultConstraintViolation();
        // 새로운 메시지로 제약 위반 생성
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}