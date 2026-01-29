package co.kr.payment.exception;



import co.kr.payment.model.dto.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;


public class GlobalExceptionHandler {
    
    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<BaseResponse<String>> paymentFailedException(PaymentFailedException e) {
        BaseResponse<String> response = new BaseResponse<>(e.getErrorCode().getCode(), e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
