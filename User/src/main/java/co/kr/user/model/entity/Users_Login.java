package co.kr.user.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Users_Login")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users_Login {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Login_IDX")
    private Long loginIdx;

    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    // 주의: 토큰 길이가 255자를 넘을 경우 TEXT 등으로 변경하거나 해시값을 저장해야 함
    @Column(name = "Token", nullable = false, length = 255)
    private String token;

    @Column(name = "Last_Used_At")
    private LocalDateTime lastUsedAt;

    @Column(name = "Revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "Revoke_Reason", length = 60)
    private String revokeReason;

    @Builder
    public Users_Login(Long usersIdx, String token, LocalDateTime lastUsedAt) {
        this.usersIdx = usersIdx;
        this.token = token;
        this.lastUsedAt = lastUsedAt;
    }

    public void logout() {
        this.lastUsedAt = LocalDateTime.now();
        this.revokedAt = LocalDateTime.now(); // 현재 시간으로 만료 처리
        this.revokeReason = "LOGOUT";         // 사유: LOGOUT
    }

    // Users_Login 클래스 안에 추가하세요
    public void updateLastUsedAt() {
        this.lastUsedAt = LocalDateTime.now();
    }
}