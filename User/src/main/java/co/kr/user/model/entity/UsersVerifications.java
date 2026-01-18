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

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
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
    @Column(name = "PurPose")
    private UsersVerificationsPurPose purPose;

    @Column(name = "Code")
    private String code;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "Verified_at")
    private LocalDateTime verifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private UsersVerificationsStatus status;

    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private int del = 0;

    @Builder
    public UsersVerifications(Long usersIdx, UsersVerificationsPurPose purPose, String code, LocalDateTime expiresAt, UsersVerificationsStatus status) {
        this.usersIdx = usersIdx;
        this.purPose = purPose;
        this.code = code;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public void confirmVerification() {
        this.status = UsersVerificationsStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }
}