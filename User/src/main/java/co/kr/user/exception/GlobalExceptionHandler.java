package co.kr.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리기(Global Exception Handler) 클래스입니다.
 * 컨트롤러 전역에서 발생하는 예외를 감지(Catch)하여 클라이언트에게 일관된 형식의 에러 응답을 반환합니다.
 * AOP 기반의 @RestControllerAdvice를 사용하여 모든 REST 컨트롤러에 적용됩니다.
 */
@RestControllerAdvice // 모든 @RestController에서 발생하는 예외를 처리하는 클래스임을 명시합니다.
public class GlobalExceptionHandler {

    /**
     * 입력값 검증 실패 예외(MethodArgumentNotValidException) 처리 핸들러
     * @Valid 또는 @Validated 어노테이션에 의해 유효성 검증이 실패했을 때 발생하는 예외를 처리합니다.
     *
     * @param ex 발생한 예외 객체
     * @return 검증 실패 내역(필드명과 에러 메시지)을 담은 Map과 HTTP 400 Bad Request 상태 코드
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 검증 실패 결과를 가져옵니다.
        BindingResult bindingResult = ex.getBindingResult();
        Map<String, String> errors = new HashMap<>();

        // 각 필드별 에러 메시지를 Map에 담습니다. (key: 필드명, value: 에러 메시지)
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        // 400 Bad Request 상태와 함께 에러 목록을 반환합니다.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * 잘못된 인자 예외(IllegalArgumentException) 처리 핸들러
     * 비즈니스 로직 수행 중 잘못된 파라미터가 전달되거나 데이터가 없을 때 주로 발생시킵니다.
     *
     * @param ex 발생한 예외 객체
     * @return 예외 메시지(String)와 HTTP 400 Bad Request 상태 코드
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        // 예외 메시지를 그대로 클라이언트에게 반환합니다.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * 잘못된 상태 예외(IllegalStateException) 처리 핸들러
     * 객체의 현재 상태가 메서드 호출을 처리하기에 적절하지 않을 때(예: 이미 탈퇴한 회원, 인증되지 않은 상태 등) 발생시킵니다.
     *
     * @param ex 발생한 예외 객체
     * @return 예외 메시지(String)와 HTTP 403 Forbidden 상태 코드
     * (참고: 상황에 따라 400 Bad Request나 409 Conflict를 사용하기도 하지만, 여기서는 권한/상태 문제로 보아 403을 사용하는 것으로 보입니다.)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        // 403 Forbidden 상태와 함께 예외 메시지를 반환합니다.
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    /**
     * 런타임 예외(RuntimeException) 처리 핸들러
     * 위에서 명시적으로 처리하지 않은 나머지 모든 런타임 예외를 처리하는 '안전망' 역할을 합니다.
     *
     * @param ex 발생한 예외 객체
     * @return 예외 메시지(String)와 HTTP 500 Internal Server Error 상태 코드
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        // 예상치 못한 서버 오류이므로 500 상태 코드를 반환합니다.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}