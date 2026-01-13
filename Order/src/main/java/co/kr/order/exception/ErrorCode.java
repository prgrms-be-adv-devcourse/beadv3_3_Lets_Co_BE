package co.kr.order.exception;

public enum ErrorCode {
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    CART_NOT_FOUND("CART_NOT_FOUND", "장바구니 정보를 찾을 수 없습니다."),
    NO_INPUT_CARD_DATA("NO_INPUT_CARD_DATA", "카드 정보를 입력해주세요."),
    NO_INPUT_ADDRESS_DATA("NO_INPUT_ADDRESS_DATA", "주소 정보를 입력해주세요."),
    NO_INPUT_ORDER_DATA("NO_INPUT_ORDER_DATA", "카드와 주소 정보를 입력해 주세요");

    private final String code;
    private final String msg;

    ErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() { return code; }
    public String getMsg() { return msg; }
}
