package co.kr.order.exception;

public enum ErrorCode {
    CART_NOT_FOUND("CART_NOT_FOUND", "장바구니 정보를 찾을 수 없습니다.");

    private final String code;
    private final String msg;

    ErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() { return code; }
    public String getMsg() { return msg; }
}
