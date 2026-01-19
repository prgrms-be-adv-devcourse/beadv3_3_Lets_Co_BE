package co.kr.user.exception; // [핵심] 패키지 위치

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * [전역 예외 처리기]
 * @RestControllerAdvice: 모든 컨트롤러(@RestController)에서 발생하는 예외를 감지하여
 * 별도의 try-catch 없이 공통된 응답 포맷으로 내려주는 역할을 합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * [비즈니스 로직 예외 처리]
     * Service 로직에서 throw new IllegalArgumentException("...") 등으로 던진 에러를 잡습니다.
     * -> 500 에러 대신 400 Bad Request로 응답
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleBusinessException(RuntimeException e) {
        log.warn("Business Exception: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    /**
     * [유효성 검사 예외 처리]
     * @Valid 어노테이션(@NotBlank, @Email 등) 위배 시 발생하는 에러를 잡습니다.
     * -> 어떤 필드가 틀렸는지 Map으로 정리해서 보여줍니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation Exception");
        Map<String, String> errors = new HashMap<>();

        // 에러가 발생한 필드명과 메시지를 추출
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * [기타 예상치 못한 에러 처리]
     * 위에서 잡지 못한 모든 에러(NullPointerException 등)는 여기서 잡아서 500으로 처리합니다.
     * -> 사용자에게는 "서버 내부 오류"라고만 알려주고, 실제 로그는 서버에 남깁니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Unhandled Exception", e);
        return ResponseEntity.internalServerError().body("서버 내부 오류가 발생했습니다. 관리자에게 문의하세요.");
    }
}