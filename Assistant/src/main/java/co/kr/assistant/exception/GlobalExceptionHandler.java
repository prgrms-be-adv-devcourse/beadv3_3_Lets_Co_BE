package co.kr.assistant.exception;

import co.kr.assistant.util.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전역에서 발생하는 예외를 감지하고 처리하는 핸들러 클래스입니다.
 * 내부 로직 노출을 막기 위해 에러 상세 내역은 서버 로그로만 남기고,
 * 클라이언트에게는 정제된 메시지를 반환합니다.
 */
@Slf4j // 서버 내부에서만 에러 원인을 추적하기 위해 로거(Logger) 추가
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직에서 의도적으로 발생시키는 런타임 예외를 처리합니다.
     * (예: 세션 만료, 잘못된 파라미터 요청 등)
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<BaseResponse<String>> handleRuntimeException(RuntimeException e) {
        // 실제 에러 원인 및 로직 정보는 서버 로그에만 기록합니다.
        log.warn("Business Logic Exception: {}", e.getMessage(), e);

        // 클라이언트에는 Service 단에서 작성한 메시지만 던지거나 정제된 메시지로 응답합니다.
        // (만약 e.getMessage() 조차 숨기고 싶다면 "잘못된 요청입니다." 와 같이 고정 텍스트로 변경 가능합니다.)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse<>("FAIL", e.getMessage()));
    }

    /**
     * @Valid 어노테이션을 통한 DTO 유효성 검증 실패 시 발생하는 예외를 처리합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<String>> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder errorMessage = new StringBuilder();

        // 사용자에게 입력 형식이 잘못되었음을 알려주기 위해, 정의된 첫 번째 에러 메시지만 노출합니다.
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errorMessage.append(fieldError.getDefaultMessage());
            break;
        }

        log.warn("Validation Exception: {}", errorMessage.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse<>("FAIL", errorMessage.toString()));
    }

    /**
     * 그 외 처리되지 않은 모든 일반적인 예외를 처리합니다. (NullPointerException, SQLException 등)
     * 이 부분에서 서버 내부 로직이나 코드 구조가 유출되는 것을 엄격히 차단합니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleException(Exception e) {
        // 상세한 에러 트레이스는 반드시 서버 내부 콘솔이나 로그 파일에만 남겨야 합니다.
        log.error("Unhandled Internal Server Exception: {}", e.getMessage(), e);

        // 클라이언트에게는 실제 발생한 예외 클래스나 쿼리, 코드 라인 번호 등을 절대 노출하지 않고
        // 매우 일반적인 메시지와 HTTP 500 상태 코드만 반환합니다.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseResponse<>("ERROR", "서버 내부에서 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }
}