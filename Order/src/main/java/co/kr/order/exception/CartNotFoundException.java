package co.kr.order.exception;

import lombok.Getter;

@Getter
public class CartNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public CartNotFoundException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
}
