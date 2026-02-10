package co.kr.payment.model.vo;

public enum PaymentStatus {
    // 결제 액션에 가까움. 상태 전이가 아님 주의.
    CHARGE,
    PAYMENT,
    REFUND
}
