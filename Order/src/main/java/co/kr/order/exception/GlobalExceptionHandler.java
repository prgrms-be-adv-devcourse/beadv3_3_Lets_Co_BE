package co.kr.order.exception;

import co.kr.order.model.dto.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseResponse<String>> handleCustomException(CustomException e) {

        BaseResponse<String> response = new BaseResponse<>(e.getCode(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        BaseResponse<String> response = new BaseResponse<>("BAD_REQUEST_VALID", errorMessage);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<BaseResponse<String>> orderNotFoundException(OrderNotFoundException e) {
        BaseResponse<String> response = new BaseResponse<>(e.getErrorCode().getCode(), e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderItemNotFoundException.class)
    public ResponseEntity<BaseResponse<String>> orderItemNotFoundException(OrderItemNotFoundException e) {
        BaseResponse<String> response = new BaseResponse<>(e.getErrorCode().getCode(), e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<BaseResponse<String>> userNotFoundException(UserNotFoundException e) {
        BaseResponse<String> response = new BaseResponse<>(e.getErrorCode().getCode(), e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<BaseResponse<String>> cartNotFoundException(CartNotFoundException e) {
        BaseResponse<String> response = new BaseResponse<>(e.getErrorCode().getCode(), e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoInputAddressDataException.class)
    public ResponseEntity<BaseResponse<String>> noInputAddressDataException(NoInputAddressDataException e) {
        BaseResponse<String> response = new BaseResponse<>(e.getErrorCode().getCode(), e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<BaseResponse<String>> productNotFoundException(ProductNotFoundException e) {
        BaseResponse<String> response = new BaseResponse<>(e.getErrorCode().getCode(), e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<BaseResponse<String>> outOfStockException(OutOfStockException e) {
        BaseResponse<String> response = new BaseResponse<>(e.getErrorCode().getCode(), e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
