package co.kr.order.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final String code;

    // ErrorCode를 쓰되, 메시지만 상세하게 바꾸고 싶은 경우
    // 예, ErrorCode.CART_NOT_FOUND, "상세하게 바꾸고싶은 메시지"
    public CustomException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.code = errorCode.getCode();
    }

    // 아예 문자열로 코드와 메시지를 직접 지정하는 경우
    public CustomException(String code, String message) {
        super(message);
        this.code = code;
    }
}