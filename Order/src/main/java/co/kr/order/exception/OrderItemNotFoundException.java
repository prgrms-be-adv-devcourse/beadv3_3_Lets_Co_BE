package co.kr.order.exception;

import lombok.Getter;

@Getter
public class OrderItemNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public OrderItemNotFoundException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
}
