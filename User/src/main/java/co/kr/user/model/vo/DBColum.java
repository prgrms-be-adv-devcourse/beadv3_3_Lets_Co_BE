package co.kr.user.model.vo;

/**
 * 데이터베이스 정렬 기준 컬럼을 정의하는 Enum 클래스입니다.
 * 관리자 서비스의 회원 목록 조회 시 어떤 필드를 기준으로 정렬할지 결정하는 데 사용됩니다.
 */
public enum DBColum {
    /** 기본 정렬 기준 (보통 생성일) */
    DEFAULT,
    /** 사용자 권한 (ADMIN, USERS, SELLER) */
    ROLE,
    /** 사용자 아이디 */
    ID,
    /** 멤버십 등급 (VIP, GOLD 등) */
    MEMBERSHIP,
    /** 사용자 이름 */
    NAME
}