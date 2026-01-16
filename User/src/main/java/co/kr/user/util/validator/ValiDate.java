package co.kr.user.util.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * [사용자 정의 날짜 검증 어노테이션]
 * @Pattern이나 @Past 같은 기본 어노테이션만으로는 해결하기 어려운
 * 복잡한 날짜 검증(형식 확인 + 미래 날짜 방지 + 유효한 날짜 여부 등)을 위해 만든 커스텀 어노테이션입니다.
 *
 * 사용 예시:
 * @ValiDate
 * private String birth;
 */
@Documented // javadoc 등의 문서 생성 시 이 어노테이션 정보도 포함되도록 설정합니다.
@Constraint(validatedBy = DateValidator.class) // [핵심] 이 어노테이션이 붙은 필드는 'DateValidator' 클래스가 검증 로직을 수행한다고 연결합니다.
@Target({ElementType.FIELD}) // 이 어노테이션은 멤버 변수(Field) 위에만 붙일 수 있습니다.
@Retention(RetentionPolicy.RUNTIME) // 컴파일 이후 런타임(실행) 시점까지 이 어노테이션 정보가 유지되어야 리플렉션 등을 통해 검증이 가능합니다.
public @interface ValiDate {

    /**
     * [기본 에러 메시지]
     * 유효성 검사에 실패했을 때 출력될 기본 메시지입니다.
     * 사용하는 쪽에서 message="..." 로 덮어씌울 수 있습니다.
     */
    String message() default "생년월일은 과거 날짜여야 하며, 올바른 형식(YYYY-MM-DD)이어야 합니다.";

    /**
     * [그룹 설정]
     * 특정 상황(예: 등록 시, 수정 시)에 따라 검증 그룹을 나눌 때 사용합니다.
     * (현재는 기본값인 빈 배열 {} 사용)
     */
    Class<?>[] groups() default {};

    /**
     * [페이로드 설정]
     * 심각도(Severity) 등 클라이언트에게 전달할 추가적인 메타데이터를 정의할 때 사용합니다.
     * (보통 잘 사용하지 않으며, 규약상 필수적으로 포함해야 하는 필드입니다.)
     */
    Class<? extends Payload>[] payload() default {};
}