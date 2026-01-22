package co.kr.order.exception;

public class OutOfStockException extends RuntimeException {
    private final ErrorCode errorCode;

    public OutOfStockException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() { return errorCode; }
}
