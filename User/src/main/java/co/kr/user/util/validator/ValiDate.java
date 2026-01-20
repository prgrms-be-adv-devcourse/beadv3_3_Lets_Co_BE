package co.kr.user.util.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 날짜 형식을 검증하기 위한 커스텀 어노테이션입니다.
 * DTO의 필드에 @ValiDate를 붙여서 사용하며, DateValidator 클래스를 통해 검증이 수행됩니다.
 */
@Documented
@Constraint(validatedBy = DateValidator.class) // 실제 검증 로직을 담당할 클래스(DateValidator)를 지정합니다.
@Target({ElementType.FIELD}) // 이 어노테이션은 필드(멤버 변수)에만 적용할 수 있습니다.
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 어노테이션 정보가 유지되어 리플렉션 등을 통해 검증이 가능하게 합니다.
public @interface ValiDate {

    // 유효성 검증 실패 시 반환할 기본 에러 메시지입니다.
    String message() default "생년월일은 과거 날짜여야 하며, 올바른 형식(YYYY-MM-DD)이어야 합니다.";

    // 상황별 유효성 검증 그룹(Groups)을 지정할 때 사용합니다. (기본값은 빈 배열)
    Class<?>[] groups() default {};

    // 심각도 등 추가적인 메타데이터(Payload)를 전달할 때 사용합니다. (기본값은 빈 배열)
    Class<? extends Payload>[] payload() default {};
}