package co.kr.user.model.vo;

/**
 * 서비스의 회원 등급(멤버십) 체계를 정의하는 Enum 클래스입니다.
 * 사용자의 구매 실적이나 가입 조건에 따라 등급을 부여하고 관리하는 데 사용됩니다.
 */
public enum UsersMembership {
    /** 최상위 등급 (VIP) */
    VIP,
    /** 우수 등급 (GOLD) */
    GOLD,
    /** 일반 등급 (SILVER) */
    SILVER,
    /** 기본 등급 (STANDARD) */
    STANDARD
}