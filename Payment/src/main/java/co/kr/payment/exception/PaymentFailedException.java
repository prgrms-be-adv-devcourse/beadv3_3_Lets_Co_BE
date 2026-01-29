package co.kr.payment.exception;

public class PaymentFailedException extends RuntimeException {
    private final ErrorCode errorCode;

    public PaymentFailedException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() { return errorCode; }
}
