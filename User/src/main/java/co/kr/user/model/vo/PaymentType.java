package co.kr.user.model.vo;

/**
 * 결제 수단 및 유형을 정의하는 Enum 클래스입니다.
 * 사용자가 어떤 방식으로 결제 또는 충전을 수행했는지 기록하는 데 사용됩니다.
 */
public enum PaymentType {
     /** 신용/체크카드 결제 */
     CARD,
     /** 무통장 입금 */
     DEPOSIT,
     /** 토스페이 간편결제 */
     TOSS_PAY
}