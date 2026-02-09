package co.kr.order.exception;

import lombok.Getter;

@Getter
public class OrderRefundedException extends RuntimeException {
    private final ErrorCode errorCode;

    public OrderRefundedException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
}