package co.kr.user.model.entity;

import co.kr.user.model.vo.PublicDel;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/**
 * 이메일 또는 휴대폰 인증 등의 본인 확인 절차 정보를 관리하는 Entity 클래스입니다.
 * 발급된 코드의 유효 기간, 인증 목적, 현재 상태 등을 추적합니다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Users_Verifications")
public class UsersVerifications {
    /** 인증 고유 식별자 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Verification_IDX")
    private Long verificationIdx;

    /** 인증을 요청한 사용자의 식별자 */
    @Column(name = "Users_IDX")
    private Long usersIdx;

    /** 인증 목적 (SIGNUP, RESET_PW, DELETE_ACCOUNT 등) */
    @Enumerated(EnumType.STRING)
    @Column(name = "Purpose", length = 20)
    private UsersVerificationsPurPose purpose;

    /** 발급된 랜덤 인증 코드 */
    @Column(name = "Code", length = 32)
    private String code;

    /** 인증 요청 생성 일시 */
    @Column(name = "Created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 인증 코드 만료 예정 일시 */
    @Column(name = "Expires_at")
    private LocalDateTime expiresAt;

    /** 실제 인증이 완료된 일시 */
    @Column(name = "Verified_at", insertable = false)
    private LocalDateTime verifiedAt;

    /** 인증 현재 상태 (PENDING, VERIFIED, EXPIRED 등) */
    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private UsersVerificationsStatus status;

    /** 해당 인증 데이터의 삭제/유효 여부 */
    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private PublicDel del;

    @Builder
    public UsersVerifications(Long usersIdx, UsersVerificationsPurPose purpose, String code, LocalDateTime expiresAt, UsersVerificationsStatus status) {
        this.usersIdx = usersIdx;
        this.purpose = purpose;
        this.code = code;
        this.expiresAt = expiresAt;
        this.status = status;
        this.del = PublicDel.ACTIVE;
    }

    /** 사용자가 입력한 코드가 확인되어 인증을 완료 처리합니다. */
    public void confirmVerification() {
        this.status = UsersVerificationsStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }
}