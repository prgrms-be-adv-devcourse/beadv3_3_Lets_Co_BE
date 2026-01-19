package co.kr.user.model.entity;

import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 인증 정보를 관리하는 엔티티 클래스입니다.
 * 이메일 인증, 휴대폰 인증 등 다양한 목적(Purpose)으로 발송된 인증 코드의 생성, 만료, 검증 상태를 추적합니다.
 */
@Entity // JPA 엔티티임을 명시합니다.
@Getter // 모든 필드의 Getter 메서드를 자동 생성합니다.
@Setter // Setter 메서드를 자동 생성합니다 (일부 비즈니스 로직에서 상태 변경을 위해 사용).
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자의 접근을 PROTECTED로 제한하여 안전성을 높입니다.
@EntityListeners(AuditingEntityListener.class) // 생성일(createdAt) 자동 관리를 위해 Auditing 기능을 활성화합니다.
@DynamicInsert // INSERT 시 null인 필드를 제외하여 DB 기본값(Default)이 적용되도록 합니다.
@Table(name = "Users_Verifications") // 데이터베이스의 "Users_Verifications" 테이블과 매핑됩니다.
public class UsersVerifications {
    /**
     * 인증 내역의 고유 식별자(PK)입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Verification_IDX")
    private Long verificationIdx;

    /**
     * 인증을 요청한 사용자의 식별자(FK)입니다.
     * 어떤 사용자에 대한 인증 요청인지 연결합니다.
     */
    @Column(name = "Users_IDX")
    private Long usersIdx;

    /**
     * 인증의 목적을 나타냅니다.
     * 예: SIGNUP(회원가입), RESET_PW(비밀번호 재설정), SELLER_SIGNUP(판매자 등록) 등
     * Enum 타입을 문자열로 저장합니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "PurPose")
    private UsersVerificationsPurPose purPose;

    /**
     * 발송된 인증 코드입니다.
     * 사용자가 입력하여 검증해야 할 문자열(난수 등)입니다.
     */
    @Column(name = "Code")
    private String code;

    /**
     * 인증 요청(코드 생성) 일시입니다.
     * JPA Auditing에 의해 자동으로 기록됩니다.
     * 수정이 불가능하도록 updatable = false로 설정되어 있습니다.
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 인증 코드의 만료 일시입니다.
     * 이 시간이 지나면 해당 코드로 인증을 시도할 수 없습니다.
     */
    @Column(name = "Expires_at")
    private LocalDateTime expiresAt;

    /**
     * 인증이 완료된 일시입니다.
     * 검증에 성공한 경우에만 기록되며, 초기값은 NULL입니다.
     */
    @Column(name = "Verified_at")
    private LocalDateTime verifiedAt;

    /**
     * 현재 인증 상태를 나타냅니다.
     * PENDING(대기), VERIFIED(인증됨), EXPIRED(만료됨) 등의 상태 값을 가집니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private UsersVerificationsStatus status;

    /**
     * 데이터 삭제 여부를 나타내는 플래그입니다.
     * 0: 정상, 1: 삭제됨 (Soft Delete)
     */
    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private int del = 0;

    /**
     * 인증 정보 생성을 위한 빌더 패턴 생성자입니다.
     * * @param usersIdx 사용자 식별자
     * @param purPose 인증 목적
     * @param code 인증 코드
     * @param expiresAt 만료 시간
     * @param status 초기 상태 (보통 PENDING)
     */
    @Builder
    public UsersVerifications(Long usersIdx, UsersVerificationsPurPose purPose, String code, LocalDateTime expiresAt, UsersVerificationsStatus status) {
        this.usersIdx = usersIdx;
        this.purPose = purPose;
        this.code = code;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    /**
     * 인증 성공 처리를 수행하는 메서드입니다.
     * 상태를 VERIFIED(인증 완료)로 변경하고, 현재 시간을 인증 완료 시간으로 기록합니다.
     */
    public void confirmVerification() {
        this.status = UsersVerificationsStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }
}