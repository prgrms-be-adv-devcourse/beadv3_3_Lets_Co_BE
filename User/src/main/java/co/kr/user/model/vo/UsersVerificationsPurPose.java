package co.kr.user.model.vo;

/**
 * 사용자 인증(본인 확인) 요청의 목적을 정의하는 Enum 클래스입니다.
 * 이메일 인증이나 SMS 인증 시 해당 인증 코드가 어떤 기능(가입, 찾기 등)을 위해 생성되었는지 식별합니다.
 */
public enum UsersVerificationsPurPose {
     /** 회원가입 시 본인 확인 */
     SIGNUP,
     /** 아이디 찾기 시 본인 확인 */
     FIND_ID,
     /** 비밀번호 분실에 따른 재설정 */
     RESET_PW,
     /** 로그인 후 비밀번호 변경 */
     CHANGE_PW,
     /** 이메일 주소 변경 시 새로운 이메일 확인 */
     CHANGE_EMAIL,
     /** 2단계 인증(Two-Factor Authentication) 로그인 */
     LOGIN_2FA,
     /** 계정 탈퇴 시 최종 본인 확인 */
     DELETE_ACCOUNT,
     /** 판매자 등록 신청 시 본인 확인 */
     SELLER_SIGNUP,
     /** 판매자 권한 해지(탈퇴) 시 본인 확인 */
     SELLER_DELETE
}