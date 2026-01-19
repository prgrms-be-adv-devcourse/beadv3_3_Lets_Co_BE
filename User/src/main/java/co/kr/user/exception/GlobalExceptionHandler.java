package co.kr.user.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * 전역 예외 처리(Global Exception Handling)를 담당하는 클래스입니다.
 * 컨트롤러 전역에서 발생하는 예외를 감지하여 일관된 형식의 응답을 반환합니다.
 */
@RestControllerAdvice // 모든 @RestController에서 발생하는 예외를 처리하는 어드바이스 클래스임을 명시합니다.
public class GlobalExceptionHandler {

    /**
     * 유효성 검사 실패 예외 처리 (@Valid)
     * @RequestBody 등의 데이터 유효성 검증 실패 시 발생하는 MethodArgumentNotValidException을 처리합니다.
     *
     * @param ex 발생한 예외 객체
     * @return ResponseEntity<String> HTTP 400 Bad Request와 첫 번째 에러 메시지 반환
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 유효성 검증 오류 중 첫 번째 오류 메시지를 추출하여 반환합니다.
        return ResponseEntity.status(400).body(ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    /**
     * 데이터 없음 예외 처리 (NoSuchElementException)
     * DB 조회 결과가 없을 때 등 발생하는 NoSuchElementException을 처리합니다.
     *
     * @param ex 발생한 예외 객체
     * @return ResponseEntity<String> HTTP 404 Not Found와 예외 메시지 반환
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    /**
     * 잘못된 인자 예외 처리 (IllegalArgumentException)
     * 부적절한 인자가 전달되었을 때 발생하는 IllegalArgumentException을 처리합니다.
     *
     * @param ex 발생한 예외 객체
     * @return ResponseEntity<String> HTTP 400 Bad Request와 예외 메시지 반환
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(400).body(ex.getMessage());
    }

    /**
     * 런타임 예외 처리 (RuntimeException)
     * 기타 예상치 못한 런타임 예외를 처리합니다.
     *
     * @param ex 발생한 예외 객체
     * @return ResponseEntity<String> HTTP 400 Bad Request와 예외 메시지 반환
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(400).body(ex.getMessage());
    }

    /**
     * 모든 예외 처리 (Exception)
     * 위에서 처리되지 않은 나머지 모든 예외를 처리합니다.
     *
     * @param ex 발생한 예외 객체
     * @return ResponseEntity<String> HTTP 500 Internal Server Error와 예외 메시지 반환
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.status(500).body(ex.getMessage());
    }
}