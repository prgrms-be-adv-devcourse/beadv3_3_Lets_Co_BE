package co.kr.order.exception;

public enum ErrorCode {
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "주문 정보를 찾을 수 없습니다"),
    ORDER_ITEM_NOT_FOUND("ORDER_ITEM_NOT_FOUND", "주문 아이템 정보를 찾을 수 없습니다"),
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    CART_NOT_FOUND("CART_NOT_FOUND", "장바구니 정보를 찾을 수 없습니다."),
    NO_INPUT_ADDRESS_DATA("NO_INPUT_ADDRESS_DATA", "주소 정보를 입력해주세요."),
    PAYMENT_FAILED("PAYMENT_FAILED", "결제를 실패했습니다."),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "제품을 찾을 수 없습니다."),
    OUT_OF_STOCK("OUT_OF_STOCK", "재고가 부족합니다."),
    ORDER_REFUND_EXCEPTION("ORDER_REFUND_EXCEPTION","주문 후처리 중 오류가 발생하여 자동 환불되었습니다.");


    private final String code;
    private final String msg;

    ErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() { return code; }
    public String getMsg() { return msg; }
}