package co.kr.assistant.exception;

import co.kr.assistant.util.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전역에서 발생하는 예외를 감지하고 처리하는 핸들러 클래스입니다.
 * @RestControllerAdvice를 사용하여 모든 컨트롤러의 예외를 중앙에서 관리합니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직에서 발생하는 런타임 예외(IllegalArgumentException, IllegalStateException)를 처리합니다.
     * 주로 잘못된 인자 전달이나 유효하지 않은 상태 접근 시 발생합니다.
     *
     * @param e 발생한 런타임 예외
     * @return 에러 메시지를 포함한 BaseResponse 객체 (HTTP 200 OK)
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<BaseResponse<String>> handleRuntimeException(RuntimeException e) {
        // 예외 메시지를 그대로 응답 메시지로 사용하여 클라이언트에게 전달
        return ResponseEntity.ok(new BaseResponse<>("FAIL", e.getMessage()));
    }

    /**
     * @Valid 어노테이션을 통한 유효성 검증 실패 시 발생하는 예외를 처리합니다.
     * DTO의 필드 제약조건 위반 내용을 상세히 반환합니다.
     *
     * @param e 유효성 검증 예외
     * @return 첫 번째 필드 에러 메시지를 포함한 BaseResponse 객체
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<String>> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder errorMessage = new StringBuilder();

        // 발생한 모든 필드 에러 중 첫 번째 에러 메시지만 추출하여 반환
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errorMessage.append(fieldError.getDefaultMessage());
            break; // 여러 에러가 있어도 하나만 표시하고 종료
        }

        return ResponseEntity.ok(new BaseResponse<>("FAIL", errorMessage.toString()));
    }

    /**
     * 그 외 처리되지 않은 모든 일반적인 예외를 처리합니다.
     *
     * @param e 발생한 예외
     * @return 서버 에러 메시지를 포함한 BaseResponse 객체
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleException(Exception e) {
        // 보안상 구체적인 에러 로그는 서버에만 남기고 클라이언트에는 일반적인 메시지 전달 가능
        // 여기서는 예외 메시지를 그대로 반환하도록 설정됨
        return ResponseEntity.ok(new BaseResponse<>("ERROR", e.getMessage()));
    }
}