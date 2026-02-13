package co.kr.user.util.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateValidator implements ConstraintValidator<ValiDate, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        try {
            LocalDate birthDate = LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
            LocalDate today = LocalDate.now();

            if (birthDate.isAfter(today)) {
                buildCustomMessage(context, "생년월일은 미래 날짜일 수 없습니다.");
                return false;
            }

            LocalDate minimumAgeDate = today.minusYears(14);

            if (birthDate.isAfter(minimumAgeDate)) {
                buildCustomMessage(context, "만 14세 미만은 가입할 수 없습니다.");
                return false;
            }

            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void buildCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}