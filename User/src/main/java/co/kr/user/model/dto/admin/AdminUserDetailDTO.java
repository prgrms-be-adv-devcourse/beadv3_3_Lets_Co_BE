package co.kr.user.model.dto.admin;

import co.kr.user.model.dto.address.AddressListDTO;
import co.kr.user.model.dto.card.CardListDTO;
import co.kr.user.model.vo.UsersInformationGender;
import co.kr.user.model.vo.UsersMembership;
import co.kr.user.model.vo.UsersRole;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 전용 회원 상세 정보 조회 응답 DTO입니다.
 * 일반적인 프로필 정보 외에도 계정 상태, 약관 동의 일시, 등록된 주소 및 카드 목록 등 모든 정보를 포함합니다.
 */
@Data
public class AdminUserDetailDTO {
    private String id;                       /** 사용자 로그인 ID */
    private LocalDateTime lockedUntil;       /** 계정 잠금 해제 일시 */
    private UsersRole role;                  /** 사용자 권한 */
    private UsersMembership membership;      /** 멤버십 등급 */
    private LocalDateTime agreeTermsAt;      /** 이용약관 동의 일시 */
    private LocalDateTime agreePrivacyAt;    /** 개인정보 처리방침 동의 일시 */
    private LocalDateTime createdAt;         /** 계정 생성 일시 */
    private LocalDateTime updatedAt;         /** 계정 정보 수정 일시 */
    private String mail;                     /** 사용자 이메일 */
    private UsersInformationGender gender;   /** 성별 */
    private BigDecimal balance;              /** 현재 잔액 */
    private String name;                     /** 사용자 이름 */
    private String phoneNumber;              /** 휴대폰 번호 */
    private String birth;                    /** 생년월일 */
    private List<AddressListDTO> addressListDTO; /** 등록된 배송지 목록 */
    private List<CardListDTO> cardListDTO;       /** 등록된 카드 목록 */
    private LocalDateTime agreeMarketingAt;  /** 마케팅 수신 동의 일시 */
}