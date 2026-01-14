package co.kr.order.exception;

import lombok.Getter;

@Getter
public class NoInputCardDataException extends RuntimeException {
    private final ErrorCode errorCode;

    public NoInputCardDataException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
}
