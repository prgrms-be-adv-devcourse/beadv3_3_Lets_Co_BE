package co.kr.user.util.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 생년월일 날짜 형식 및 유효성(미래 날짜 불가, 만 14세 미만 불가 등)을 검증하기 위한 커스텀 어노테이션입니다.
 * 필드 레벨(ElementType.FIELD)에 적용되어 날짜 문자열(String)을 검사합니다.
 */
@Documented
@Constraint(validatedBy = DateValidator.class) // 실제 검증은 DateValidator 클래스에서 수행
@Target({ElementType.FIELD}) // 필드 위에 선언 가능
@Retention(RetentionPolicy.RUNTIME) // 런타임에 유지됨
public @interface ValiDate {
    // 유효성 검사 실패 시 반환할 기본 메시지
    String message() default "생년월일은 과거 날짜여야 하며, 올바른 형식(YYYY-MM-DD)이어야 합니다.";

    // 유효성 검증 그룹 설정 (기본값: 빈 배열)
    Class<?>[] groups() default {};

    // 페이로드 설정 (기본값: 빈 배열)
    Class<? extends Payload>[] payload() default {};
}