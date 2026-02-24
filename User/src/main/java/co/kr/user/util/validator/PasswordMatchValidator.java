package co.kr.user.util.validator;

import co.kr.user.model.dto.register.RegisterReq;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @PasswordMatch 어노테이션의 유효성 검증 로직을 실제로 수행하는 클래스입니다.
 * 회원가입 등의 요청에서 비밀번호(pw)와 비밀번호 확인(pwCheck) 필드가 일치하는지 검사합니다.
 */
public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, RegisterReq> {

    /**
     * 검증 로직을 구현한 메서드입니다.
     * @param value 검증할 객체 (여기서는 RegisterReq DTO)
     * @param context 유효성 검증 컨텍스트
     * @return 비밀번호가 일치하면 true, 아니면 false
     */
    @Override
    public boolean isValid(RegisterReq value, ConstraintValidatorContext context) {
        // DTO에서 비밀번호와 비밀번호 확인 값을 가져옵니다.
        String pw = value.getPw();
        String pwCheck = value.getPwCheck();

        // 비밀번호가 null이 아니고, 두 값이 서로 같은지 비교합니다.
        boolean isValid = pw != null && pw.equals(pwCheck);

        // 검증에 실패했을 경우 (비밀번호가 다르거나 null인 경우)
        if (!isValid) {
            // 기본 제약 조건 위반 메시지(어노테이션의 message 속성)를 비활성화합니다.
            context.disableDefaultConstraintViolation();

            // "pwCheck" 필드에 대해 커스텀 에러 메시지를 바인딩합니다.
            // 이렇게 하면 에러가 전체 객체 레벨이 아닌 pwCheck 필드의 에러로 처리되어 클라이언트가 인지하기 쉽습니다.
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("pwCheck")
                    .addConstraintViolation();
        }

        return isValid;
    }
}