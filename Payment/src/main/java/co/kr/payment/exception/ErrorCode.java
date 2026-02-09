package co.kr.payment.exception;

public enum ErrorCode {
    PAYMENT_FAILED("PAYMENT_FAILED", "결제를 실패했습니다."),
    PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND", "결제 내역을 찾을 수 없습니다."),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "주문 정보를 찾을 수 없습니다.");

    private final String code;
    private final String msg;

    ErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() { return code; }
    public String getMsg() { return msg; }
}
