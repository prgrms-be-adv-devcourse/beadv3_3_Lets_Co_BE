package co.kr.user.util.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 비밀번호와 비밀번호 확인 필드가 일치하는지 검증하기 위한 커스텀 어노테이션입니다.
 * 클래스 레벨(ElementType.TYPE)에 적용되어 두 개의 필드를 비교합니다.
 */
@Documented
@Constraint(validatedBy = PasswordMatchValidator.class) // 실제 검증 로직은 PasswordMatchValidator 클래스에서 수행
@Target({ElementType.TYPE}) // 이 어노테이션은 클래스(타입) 위에 선언해야 함
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 어노테이션 정보가 유지되어야 함
public @interface PasswordMatch {
    // 기본 에러 메시지
    String message() default "비밀번호가 일치하지 않습니다.";

    // 유효성 검증 그룹 설정 (기본값: 빈 배열)
    Class<?>[] groups() default {};

    // 페이로드 설정 (심각도 등 메타데이터 전달용, 기본값: 빈 배열)
    Class<? extends Payload>[] payload() default {};
}