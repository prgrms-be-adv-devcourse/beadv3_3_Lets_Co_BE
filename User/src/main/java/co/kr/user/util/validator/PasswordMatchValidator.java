package co.kr.user.util.validator;

import co.kr.user.model.dto.register.RegisterReq;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, RegisterReq> {

    @Override
    public boolean isValid(RegisterReq value, ConstraintValidatorContext context) {
        String pw = value.getPw();
        String pwCheck = value.getPw();

        boolean isValid = pw != null && pw.equals(pwCheck);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("pwCheck")
                    .addConstraintViolation();
        }

        return isValid;
    }
}