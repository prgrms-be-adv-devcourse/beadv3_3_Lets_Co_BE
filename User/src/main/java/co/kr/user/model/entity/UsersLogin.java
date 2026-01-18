package co.kr.user.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Users_Login")
public class UsersLogin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Login_IDX")
    private Long loginIdx;

    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    @Column(name = "Token", nullable = false, length = 255)
    private String token;

    @Column(name = "Last_Used_At")
    private LocalDateTime lastUsedAt;

    @Column(name = "Revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "Revoke_Reason", length = 60)
    private String revokeReason;

    @Builder
    public UsersLogin(Long usersIdx, String token, LocalDateTime lastUsedAt) {
        this.usersIdx = usersIdx;
        this.token = token;
        this.lastUsedAt = lastUsedAt;
    }

    public void logout() {
        this.lastUsedAt = LocalDateTime.now();
        this.revokedAt = LocalDateTime.now();
        this.revokeReason = "LOGOUT";
    }

    public void maturity() {
        this.revokedAt = LocalDateTime.now();
        this.revokeReason = "MATURITY";
    }

    public void updateToken(String token) {
        this.token = token;
        this.lastUsedAt = null;
        this.revokedAt = null;
        this.revokeReason = null;
    }

    public void lockToken(LocalDateTime revokedAt, String revokeReason) {
        this.revokedAt = revokedAt;
        this.revokeReason = revokeReason;
    }

    public void updateLastUsedAt() {
        this.lastUsedAt = LocalDateTime.now();
    }
}