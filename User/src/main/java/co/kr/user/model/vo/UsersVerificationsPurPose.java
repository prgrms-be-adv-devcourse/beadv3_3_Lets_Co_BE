package co.kr.user.model.vo;

/**
 * 인증 요청의 목적(Purpose)을 정의하는 Enum 클래스입니다.
 * 이메일이나 SMS 등으로 발송되는 인증 코드가 어떤 행위를 위해 요청되었는지 식별합니다.
 */
public enum UsersVerificationsPurPose {
    /**
     * 회원가입 인증
     */
    SIGNUP,

    /**
     * 비밀번호 재설정 (비밀번호 분실 시)
     */
    RESET_PW,

    /**
     * 비밀번호 변경 (로그인 상태에서 정보 수정 시)
     */
    CHANGE_PW,

    /**
     * 이메일(아이디) 변경 인증
     */
    CHANGE_EMAIL,

    /**
     * 로그인 2단계 인증 (2FA)
     */
    LOGIN_2FA,

    /**
     * 회원 탈퇴 확인 인증
     */
    DELETE_ACCOUNT,

    /**
     * 판매자 등록(전환) 인증
     */
    SELLER_SIGNUP
}