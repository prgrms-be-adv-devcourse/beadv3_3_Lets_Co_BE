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

            return birthDate.isBefore(LocalDate.now());

        } catch (DateTimeParseException e) {
            return false;
        }
    }
}