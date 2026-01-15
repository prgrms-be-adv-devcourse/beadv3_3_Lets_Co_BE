package co.kr.order.exception;

public class ProductNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public ProductNotFoundException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() { return errorCode; }
}
