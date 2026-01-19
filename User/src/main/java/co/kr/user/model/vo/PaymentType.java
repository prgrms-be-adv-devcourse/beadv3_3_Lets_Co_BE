package co.kr.user.model.vo;

/**
 * 결제 수단(유형)을 정의하는 Enum 클래스입니다.
 * 사용자가 결제나 충전 시 선택한 지불 방법을 나타냅니다.
 */
public enum PaymentType {
    /**
     * 신용카드 및 체크카드 결제
     */
    CARD,

    /**
     * 무통장 입금 또는 계좌 이체 (예치금 충전 등)
     */
    DEPOSIT,

    /**
     * 토스페이(Toss Pay) 간편 결제
     */
    TOSS_PAY
}