package co.kr.order.exception;

import lombok.Getter;

@Getter
public class NoInputOrderDataException extends RuntimeException {
    private final ErrorCode errorCode;

    public NoInputOrderDataException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
}
