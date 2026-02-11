package co.kr.user.model.entity;

import co.kr.user.model.vo.UserDel;
import co.kr.user.model.vo.UsersMembership;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.util.CryptoConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.function.BiFunction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Users_IDX")
    private Long usersIdx;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "ID", nullable = false, length = 254)
    private String id;

    @Column(name = "PW", nullable = false)
    private String pw;

    @Column(name = "Failed_Login_Attempts", nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private Integer failedLoginAttempts;

    @Column(name = "Locked_Until")
    private LocalDateTime lockedUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false, length = 20)
    private UsersRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "Membership", nullable = false, length = 20)
    private UsersMembership membership;

    @Column(name = "agree_Terms_at", insertable = false, updatable = false)
    private LocalDateTime agreeTermsAt;

    @Column(name = "agree_Privacy_at", insertable = false, updatable = false)
    private LocalDateTime agreePrivacyAt;

    @Column(name = "Created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private UserDel del;

    @Builder
    public Users(String id, String pw) {
        this.id = id;
        this.pw = pw;
        this.failedLoginAttempts = 0;
        this.role = UsersRole.USERS;
        this.membership = UsersMembership.STANDARD;
        this.del = UserDel.PENDING;
    }

    public void checkAccountStatus() {
        if (this.del == UserDel.DELETED) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        if (this.del == UserDel.PENDING) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }
    }

    public boolean isLocked() {
        return this.lockedUntil != null && this.lockedUntil.isAfter(LocalDateTime.now());
    }

    public void handleLoginFailure(int maxAttempts, int lockMinutes) {
        this.failedLoginAttempts += 1;
        if (this.failedLoginAttempts >= maxAttempts) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(lockMinutes);
        }
    }

    public void completeLogin() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public void changePassword(String newEncodedPw, String rawCurrentPw, BiFunction<String, String, Boolean> passwordChecker) {
        if (passwordChecker.apply(rawCurrentPw, this.pw)) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 다르게 설정해야 합니다.");
        }
        this.pw = newEncodedPw;
        this.completeLogin(); // 실패 횟수 및 잠금 상태 초기화 로직이 세트로 동작
    }

    public void updateMembership(UsersMembership membership) {
        this.membership = membership;
    }

    public void activateUsers() {
        this.del = UserDel.ACTIVE;
    }

    public void deleteUsers(String deletedId) {
        this.id = deletedId;
        this.del = UserDel.DELETED;
    }

    public void assignRole(UsersRole role) {
        this.role = role;
    }

    public void suspendUser(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
}