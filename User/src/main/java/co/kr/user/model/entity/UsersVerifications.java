package co.kr.user.model.entity;

import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Users_Verifications")
public class UsersVerifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Verification_IDX")
    private Long verificationIdx;

    @Column(name = "Users_IDX")
    private Long usersIdx;

    @Enumerated(EnumType.STRING)
    @Column(name = "Purpose", length = 20)
    private UsersVerificationsPurPose purpose;

    @Column(name = "Code", length = 32)
    private String code;

    @Column(name = "Created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "Verified_at", insertable = false)
    private LocalDateTime verifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private UsersVerificationsStatus status;

    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private int del;

    @Builder
    public UsersVerifications(Long usersIdx, UsersVerificationsPurPose purpose, String code, LocalDateTime expiresAt, UsersVerificationsStatus status) {
        this.usersIdx = usersIdx;
        this.purpose = purpose;
        this.code = code;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public void confirmVerification() {
        this.status = UsersVerificationsStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }
}