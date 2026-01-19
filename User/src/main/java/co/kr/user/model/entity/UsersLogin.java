package co.kr.user.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 사용자의 로그인 세션 및 리프레시 토큰(Refresh Token) 정보를 관리하는 엔티티 클래스입니다.
 * JWT 기반 인증 방식에서 액세스 토큰 재발급을 위한 리프레시 토큰의 저장, 갱신, 만료, 폐기(Revoke) 상태를 추적합니다.
 */
@Entity // JPA 엔티티임을 명시합니다.
@Getter // 모든 필드의 Getter 메서드를 자동 생성합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자의 접근 제어자를 PROTECTED로 설정하여 안전성을 높입니다.
@Table(name = "Users_Login") // 데이터베이스의 "Users_Login" 테이블과 매핑됩니다.
public class UsersLogin {
    /**
     * 로그인/토큰 이력의 고유 식별자(PK)입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Login_IDX")
    private Long loginIdx;

    /**
     * 해당 토큰을 소유한 사용자의 식별자(FK)입니다.
     * Users 테이블의 PK를 참조합니다.
     */
    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    /**
     * 발급된 리프레시 토큰(Refresh Token) 문자열입니다.
     * 액세스 토큰 갱신 요청 시 클라이언트가 제출한 토큰과 대조하는 데 사용됩니다.
     * (NULL 비허용)
     */
    @Column(name = "Token", nullable = false, length = 255)
    private String token;

    /**
     * 토큰이 마지막으로 사용된 일시입니다.
     * 토큰 회전(Rotation) 정책이나 비정상적인 접근 탐지 등을 위해 사용될 수 있습니다.
     */
    @Column(name = "Last_Used_At")
    private LocalDateTime lastUsedAt;

    /**
     * 토큰이 폐기(Revoke)된 일시입니다.
     * 로그아웃, 만료, 보안 이슈 등으로 인해 더 이상 사용할 수 없는 토큰임을 표시합니다.
     * 이 값이 NULL이 아니면 해당 토큰은 유효하지 않은 것으로 간주됩니다.
     */
    @Column(name = "Revoked_at")
    private LocalDateTime revokedAt;

    /**
     * 토큰 폐기 사유입니다.
     * 예: "LOGOUT" (로그아웃), "MATURITY" (자연 만료/갱신), "LOCKED" (관리자 차단) 등
     */
    @Column(name = "Revoke_Reason", length = 60)
    private String revokeReason;

    /**
     * 로그인 성공 시 토큰 정보를 저장하기 위한 빌더 생성자입니다.
     *
     * @param usersIdx 사용자 식별자
     * @param token 발급된 리프레시 토큰
     * @param lastUsedAt 초기 사용 일시 (보통 생성 시점)
     */
    @Builder
    public UsersLogin(Long usersIdx, String token, LocalDateTime lastUsedAt) {
        this.usersIdx = usersIdx;
        this.token = token;
        this.lastUsedAt = lastUsedAt;
    }

    /**
     * 로그아웃 처리 메서드입니다.
     * 토큰을 즉시 폐기 상태로 변경하고, 사유를 "LOGOUT"으로 기록합니다.
     */
    public void logout() {
        this.lastUsedAt = LocalDateTime.now(); // 마지막 사용 시점을 현재로 갱신
        this.revokedAt = LocalDateTime.now();  // 폐기 시점 기록
        this.revokeReason = "LOGOUT";          // 폐기 사유 기록
    }

    /**
     * 토큰 만료(갱신) 처리 메서드입니다.
     * 리프레시 토큰 회전(Rotation) 시, 기존 토큰을 만료 처리하고 사유를 "MATURITY"로 기록합니다.
     */
    public void maturity() {
        this.revokedAt = LocalDateTime.now();
        this.revokeReason = "MATURITY";
    }

    /**
     * 토큰을 갱신하는 메서드입니다.
     * 리프레시 토큰 회전(Rotation) 후 새로 발급된 토큰 값으로 엔티티를 업데이트합니다.
     * 기존의 폐기 정보(revokedAt, revokeReason)는 초기화되어 새 토큰이 유효한 상태가 됩니다.
     *
     * @param token 새로 발급된 리프레시 토큰
     */
    public void updateToken(String token) {
        this.token = token;
        this.lastUsedAt = null;   // 새 토큰이므로 사용 이력 초기화
        this.revokedAt = null;    // 유효한 상태로 변경
        this.revokeReason = null; // 폐기 사유 초기화
    }

    /**
     * 관리자 또는 시스템에 의해 강제로 토큰을 잠금(폐기) 처리하는 메서드입니다.
     *
     * @param revokedAt 폐기 일시
     * @param revokeReason 폐기 사유 (예: "LOCKED", "ADMIN_BLOCK")
     */
    public void lockToken(LocalDateTime revokedAt, String revokeReason) {
        this.revokedAt = revokedAt;
        this.revokeReason = revokeReason;
    }

    /**
     * 토큰 사용 시점을 갱신하는 메서드입니다.
     * 액세스 토큰 재발급 요청 등이 있을 때 호출되어 마지막 활동 시간을 기록합니다.
     */
    public void updateLastUsedAt() {
        this.lastUsedAt = LocalDateTime.now();
    }
}