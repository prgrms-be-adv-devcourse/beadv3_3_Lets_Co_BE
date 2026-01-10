package co.kr.order.global.exception;

import co.kr.order.domain.model.dto.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<BaseResponse<String>> cartNotFoundException(CartNotFoundException e) {
        BaseResponse<String> response = new BaseResponse<>(e.getErrorCode().getCode(), e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
