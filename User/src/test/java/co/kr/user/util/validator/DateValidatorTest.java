package co.kr.user.util.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * DateValidator(java/co/kr/user/util/validator/DateValidator.java) 단위 테스트
 */
@DisplayName("DateValidator 단위 테스트")
class DateValidatorTest {

    private DateValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new DateValidator();
        // 커스텀 메시지 생성을 위한 Mock 설정
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    }

    @Test
    @DisplayName("유효한 생년월일 테스트: 만 14세 이상의 과거 날짜는 통과해야 함")
    void validDateTest() {
        // 1990-01-01은 유효함
        assertTrue(validator.isValid("1990-01-01", context));
    }

    @Test
    @DisplayName("미래 날짜 검증 테스트: 오늘보다 이후 날짜는 실패해야 함")
    void futureDateTest() {
        String futureDate = LocalDate.now().plusDays(1).toString();
        assertFalse(validator.isValid(futureDate, context));
    }

    @Test
    @DisplayName("만 14세 미만 검증 테스트: 기준보다 어린 나이는 실패해야 함")
    void underAgeTest() {
        // 오늘로부터 13년 전 날짜는 만 14세 미만임
        String underAgeDate = LocalDate.now().minusYears(13).toString();
        assertFalse(validator.isValid(underAgeDate, context));
    }
}