package co.kr.user.exception;

import co.kr.user.util.BaseResponse;
import org.springframework.http.HttpStatus;
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
     * 비즈니스 로직에서 발생하는 런타임 예외를 처리합니다.
     * 클라이언트의 잘못된 요청으로 인한 것이므로 HTTP 400 Bad Request를 반환합니다.
     *
     * @param e 발생한 런타임 예외
     * @return 에러 메시지를 포함한 BaseResponse 객체 (HTTP 400 Bad Request)
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<BaseResponse<String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BaseResponse<>("FAIL", e.getMessage()));
    }

    /**
     * @Valid 어노테이션을 통한 유효성 검증 실패 시 발생하는 예외를 처리합니다.
     * 입력값이 잘못된 것이므로 HTTP 400 Bad Request를 반환합니다.
     *
     * @param e 유효성 검증 예외
     * @return 첫 번째 필드 에러 메시지를 포함한 BaseResponse 객체 (HTTP 400 Bad Request)
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

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BaseResponse<>("FAIL", errorMessage.toString()));
    }

    /**
     * 그 외 처리되지 않은 모든 일반적인 예외를 처리합니다.
     * 서버 측의 예상치 못한 오류이므로 HTTP 500 Internal Server Error를 반환합니다.
     *
     * @param e 발생한 예외
     * @return 서버 에러 메시지를 포함한 BaseResponse 객체 (HTTP 500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleException(Exception e) {
        // 보안상 구체적인 에러 로그는 서버에만 남기고 클라이언트에는 일반적인 메시지 전달하는 것이 좋습니다.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new BaseResponse<>("ERROR", "서버 내부 오류가 발생했습니다."));
        // 기존처럼 원본 메시지를 보내려면 e.getMessage()를 사용하세요.
    }
}