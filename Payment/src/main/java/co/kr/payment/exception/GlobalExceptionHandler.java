package co.kr.payment.exception;

import co.kr.payment.model.dto.response.BaseResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<BaseResponse<String>> handlePaymentFailed(PaymentFailedException e) {
        return new ResponseEntity<>(
                new BaseResponse<>(e.getErrorCode().getCode(), e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<String>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .orElse(ErrorCode.INVALID_INPUT_VALUE.getMsg());

        return new ResponseEntity<>(
                new BaseResponse<>(ErrorCode.INVALID_INPUT_VALUE.getCode(), message),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<String>> handleMissingParam(MissingServletRequestParameterException e) {
        return new ResponseEntity<>(
                new BaseResponse<>(ErrorCode.INVALID_INPUT_VALUE.getCode(), e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<String>> handleNotReadable(HttpMessageNotReadableException e) {
        return new ResponseEntity<>(
                new BaseResponse<>(ErrorCode.INVALID_INPUT_VALUE.getCode(), "요청 본문을 읽을 수 없습니다."),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<BaseResponse<String>> handleFeign(FeignException e) {
        log.error("내부 서비스 통신 실패: status={}, message={}", e.status(), e.getMessage(), e);
        return new ResponseEntity<>(
                new BaseResponse<>(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "내부 서비스 통신 중 오류가 발생했습니다."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleException(Exception e) {
        log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new BaseResponse<>(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMsg()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
