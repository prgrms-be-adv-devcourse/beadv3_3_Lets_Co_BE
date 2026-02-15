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

/**
 * 서비스의 핵심 사용자 계정 정보를 관리하는 Entity 클래스입니다.
 * 인증(로그인), 권한 관리, 계정 상태(잠금, 삭제) 등의 기능을 담당합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert // insert 시 null인 필드 제외 (DB 기본값 활용)
@Table(name = "Users")
public class Users {
    /** 사용자 고유 식별자 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Users_IDX")
    private Long usersIdx;

    /** 로그인 ID (결정적 암호화 적용으로 DB 내 검색 가능) */
    @Convert(converter = CryptoConverter.DeterministicConverter.class)
    @Column(name = "ID", nullable = false, length = 254)
    private String id;

    /** BCrypt로 암호화된 비밀번호 */
    @Column(name = "PW", nullable = false)
    private String pw;

    /** 로그인 실패 횟수 (계정 잠금 처리용) */
    @Column(name = "Failed_Login_Attempts", nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private Integer failedLoginAttempts;

    /** 계정 잠금 해제 일시 */
    @Column(name = "Locked_Until")
    private LocalDateTime lockedUntil;

    /** 사용자 역할 권한 (USERS, ADMIN, SELLER) */
    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false, length = 20)
    private UsersRole role;

    /** 멤버십 등급 (STANDARD, SILVER, GOLD, VIP) */
    @Enumerated(EnumType.STRING)
    @Column(name = "Membership", nullable = false, length = 20)
    private UsersMembership membership;

    /** 이용약관 동의 일시 */
    @Column(name = "agree_Terms_at", insertable = false, updatable = false)
    private LocalDateTime agreeTermsAt;

    /** 개인정보 처리방침 동의 일시 */
    @Column(name = "agree_Privacy_at", insertable = false, updatable = false)
    private LocalDateTime agreePrivacyAt;

    /** 계정 생성 일시 */
    @Column(name = "Created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 계정 정보 수정 일시 */
    @Column(name = "Updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /** 삭제/활성 상태 (0: ACTIVE, 1: DELETED, 2: PENDING) */
    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private UserDel del;

    @Builder
    public Users(String id, String pw) {
        this.id = id;
        this.pw = pw;
        this.failedLoginAttempts = 0;
        this.role = UsersRole.USERS;
        this.membership = UsersMembership.STANDARD;
        this.del = UserDel.PENDING; // 최초 생성 시 인증 대기 상태
    }

    /** 계정의 유효 상태를 체크합니다. 탈퇴나 미인증 상태 시 예외를 발생시킵니다. */
    public void checkAccountStatus() {
        if (this.del == UserDel.DELETED) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        if (this.del == UserDel.PENDING) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }
    }

    /** 현재 계정이 잠금 상태인지 확인합니다. */
    public boolean isLocked() {
        return this.lockedUntil != null && this.lockedUntil.isAfter(LocalDateTime.now());
    }

    /** 로그인 실패 시 횟수를 증가시키고 필요 시 잠금을 설정합니다. */
    public void handleLoginFailure(int maxAttempts, int lockMinutes) {
        this.failedLoginAttempts += 1;
        if (this.failedLoginAttempts >= maxAttempts) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(lockMinutes);
        }
    }

    /** 로그인 성공 시 실패 횟수 및 잠금 상태를 초기화합니다. */
    public void completeLogin() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    /** 비밀번호를 변경합니다. 현재 비밀번호와 동일한지 체크하는 로직을 포함합니다. */
    public void changePassword(String newEncodedPw, String rawCurrentPw, BiFunction<String, String, Boolean> passwordChecker) {
        if (passwordChecker.apply(rawCurrentPw, this.pw)) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 다르게 설정해야 합니다.");
        }
        this.pw = newEncodedPw;
        this.completeLogin(); // 변경 성공 시 잠금 상태도 함께 초기화
    }

    /** 멤버십 등급을 업데이트합니다. */
    public void updateMembership(UsersMembership membership) {
        this.membership = membership;
    }

    /** 계정 상태를 활성(ACTIVE)으로 변경합니다. */
    public void activateUsers() {
        this.del = UserDel.ACTIVE;
    }

    /** 계정을 논리적으로 삭제합니다. 중복 방지를 위해 ID에 탈퇴 표식을 추가합니다. */
    public void deleteUsers(String deletedId) {
        this.id = deletedId;
        this.del = UserDel.DELETED;
    }

    /** 사용자 역할을 할당합니다. (예: 판매자 승인 시 SELLER로 변경) */
    public void assignRole(UsersRole role) {
        this.role = role;
    }

    /** 관리자 등에 의해 계정을 강제 정지 처리합니다. */
    public void suspendUser(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
}