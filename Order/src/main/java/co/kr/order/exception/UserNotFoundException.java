package co.kr.order.exception;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
}
