package co.kr.order.exception;

import lombok.Getter;

@Getter
public class NoInputAddressDataException extends RuntimeException {
    private final ErrorCode errorCode;

    public NoInputAddressDataException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
}
