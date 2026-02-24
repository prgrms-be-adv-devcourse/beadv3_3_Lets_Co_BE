package co.kr.user.util.validator;

import co.kr.user.model.dto.register.RegisterReq;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * PasswordMatchValidator 단위 테스트
 * 수정 사항: Jakarta Validation의 Fluent API(메서드 체이닝) 모킹 완성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordMatchValidator 단위 테스트")
class PasswordMatchValidatorTest {

    @InjectMocks
    private PasswordMatchValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeContext;

    @Test
    @DisplayName("비밀번호 일치 테스트: 두 값이 같으면 true를 반환해야 함")
    void passwordMatchTest() {
        // Given
        RegisterReq req = new RegisterReq();
        req.setPw("Password123!");
        req.setPwCheck("Password123!");

        // When & Then
        assertTrue(validator.isValid(req, context));
    }

    @Test
    @DisplayName("비밀번호 불일치 테스트: 두 값이 다르면 false를 반환하고 에러 메시지를 설정해야 함")
    void passwordMismatchTest() {
        // Given
        RegisterReq req = new RegisterReq();
        req.setPw("Password123!");
        req.setPwCheck("Different456!");

        // [중요] 메서드 체이닝 모킹: 각 단계가 null을 반환하지 않도록 연결합니다.
        // [추가] 기본 메시지 템플릿이 null이 아닌 값을 반환하도록 설정
        when(context.getDefaultConstraintMessageTemplate()).thenReturn("비밀번호가 일치하지 않습니다.");
        // 1. buildConstraintViolationWithTemplate -> builder 반환
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        // 2. addPropertyNode -> nodeContext 반환
        when(builder.addPropertyNode(anyString())).thenReturn(nodeContext);
        // 3. addConstraintViolation -> 최종적으로 context 반환 (혹은 void 계열)
        when(nodeContext.addConstraintViolation()).thenReturn(context);

        // When
        boolean result = validator.isValid(req, context);

        // Then
        assertFalse(result);
        // 기본 에러 메시지가 비활성화 되었는지 확인
        verify(context).disableDefaultConstraintViolation();
        // 커스텀 에러 메시지가 생성되었는지 확인
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }
}