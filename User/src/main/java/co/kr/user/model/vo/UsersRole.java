package co.kr.user.model.vo;

/**
 * 시스템 내에서의 사용자 역할 및 권한을 정의하는 Enum 클래스입니다.
 * Spring Security와 연동하여 API 접근 제어 및 기능 권한을 부여하는 기준이 됩니다.
 */
public enum UsersRole {
     /** 일반 사용자 권한 */
     USERS,
     /** 시스템 관리자 권한 */
     ADMIN,
     /** 입점 판매자 권한 */
     SELLER
}