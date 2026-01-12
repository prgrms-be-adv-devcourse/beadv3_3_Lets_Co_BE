package co.kr.user.model.entity;

import co.kr.user.model.vo.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
@Table(name = "Users_Login")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Users_IDX")
    private Long usersIdx;

    @Column(name = "ID", nullable = false, length = 254)
    private String loginId;

    @Column(name = "PW", nullable = false)
    private String password;

    @Column(name = "Failed_Login_Attempts", nullable = false)
    @ColumnDefault("0")
    private Integer failedLoginAttempts = 0;

    @Column(name = "Locked_Until")
    private LocalDateTime lockedUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false, length = 20)
    @ColumnDefault("'Users'")
    private UserRole role = UserRole.USERS;

    @Column(name = "Balance", nullable = false, precision = 19, scale = 2)
    @ColumnDefault("0")
    private BigDecimal balance = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "agree_Terms_at", nullable = false, updatable = false)
    private LocalDateTime agreeTermsAt;

    @CreatedDate
    @Column(name = "agree_Privacy_at", nullable = false, updatable = false)
    private LocalDateTime agreePrivacyAt;

    @Column(name = "agree_Marketing_at")
    private LocalDateTime agreeMarketingAt;

    @CreatedDate
    @Column(name = "Created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "Updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private Boolean del = false;

    @Builder
    public User(String loginId, String password, LocalDateTime agreeMarketingAt) {
        this.loginId = loginId;
        this.password = password;
        this.agreeMarketingAt = agreeMarketingAt;
    }
}