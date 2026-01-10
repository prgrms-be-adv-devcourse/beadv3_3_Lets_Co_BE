package co.kr.order.global.exception;

public class CartNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public CartNotFoundException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() { return errorCode; }
}
