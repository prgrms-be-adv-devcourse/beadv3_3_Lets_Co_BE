package co.kr.user.model.entity;

import co.kr.user.model.vo.UsersMembership;
import co.kr.user.model.vo.UsersRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;


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
    private int del;

    @Builder
    public Users(String id, String pw) {
        this.id = id;
        this.pw = pw;
        this.failedLoginAttempts = 0;
        this.role = UsersRole.USERS;
        this.membership = UsersMembership.STANDARD;
        this.agreeTermsAt = null;
        this.agreePrivacyAt = null;
        this.createdAt = null;
        this.updatedAt = null;
        this.del = 2;
    }

    public void checkAccountStatus() {
        if (this.del == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        if (this.del == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }
        // 추가적인 공통 검증(계정 잠금 등)을 여기에 포함할 수 있습니다.
    }

    public void increaseLoginFailCount() {
        this.failedLoginAttempts += 1;
    }

    public void lockAccount() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = LocalDateTime.now().plusMinutes(15);
    }

    public void completeLogin() {
        this.failedLoginAttempts = 0;
    }

    public void changePassword(String pw) {
        this.pw = pw;
        this.failedLoginAttempts = 0;
    }

    public void updateMembership(UsersMembership membership) {
        this.membership = membership;
    }

    public void activateUsers() {
        this.del = 0;
    }

    public void deleteUsers() {
        this.del = 1;
    }

    public void assignRole(UsersRole role) {
        this.role = role;
    }

    public void suspendUser(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public void withdrawUser() {
        this.del = 1;
    }
}