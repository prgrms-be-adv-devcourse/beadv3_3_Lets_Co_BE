package co.kr.payment.exception;

public enum ErrorCode {
    // 공통
    INVALID_INPUT_VALUE("INVALID_INPUT_VALUE", "잘못된 입력 값입니다."),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    USER_MISMATCH("USER_MISMATCH", "유효한 유저가 아닙니다."),

    // 결제 승인 관련
    ALREADY_PAID("ALREADY_PAID", "이미 결제된 주문입니다."),
    PAYMENT_FAILED("PAYMENT_FAILED", "결제 승인에 실패했습니다."),
    PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND", "결제 내역을 찾을 수 없습니다."),
    PAYMENT_KEY_NOT_FOUND("PAYMENT_KEY_NOT_FOUND", "토스페이 PaymentKey가 존재하지 않습니다."),
    CHARGE_FAILED("CHARGE_FAILED", "예치금 충전에 실패했습니다."),

    // 결제 취소 관련
    PAYMENT_CANCEL_FAILED("PAYMENT_CANCEL_FAILED", "결제 취소 요청이 실패했습니다."),
    ALREADY_CANCELLED_PAYMENT("ALREADY_CANCELLED_PAYMENT", "이미 취소된 결제 건입니다."),

    // 주문 관련
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "주문 정보를 찾을 수 없습니다."),
    ORDER_AMOUNT_MISMATCH("ORDER_AMOUNT_MISMATCH", "주문 금액과 결제 금액이 일치하지 않습니다.");

    private final String code;
    private final String msg;

    ErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() { return code; }
    public String getMsg() { return msg; }
}
