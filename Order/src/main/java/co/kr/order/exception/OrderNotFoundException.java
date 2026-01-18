package co.kr.order.exception;

import lombok.Getter;

@Getter
public class OrderNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public OrderNotFoundException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
}
