package co.kr.user.model.entity;

import co.kr.user.model.vo.UsersRole;
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

/**
 * 사용자(회원)의 핵심 계정 정보를 관리하는 엔티티 클래스입니다.
 * 로그인 아이디, 비밀번호, 권한, 상태(활성/정지/탈퇴), 잔액 등 계정의 수명 주기와 직접 관련된 데이터를 다룹니다.
 */
@Entity // JPA 엔티티임을 명시합니다. DB 테이블과 매핑됩니다.
@Getter // Lombok을 사용하여 모든 필드의 Getter 메서드를 자동 생성합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자를 생성하되, 외부에서 무분별한 생성을 막기 위해 접근 제어자를 PROTECTED로 설정합니다.
@EntityListeners(AuditingEntityListener.class) // 생성일(createdAt), 수정일(updatedAt)을 자동으로 관리하기 위해 JPA Auditing 리스너를 등록합니다.
@DynamicInsert // INSERT 쿼리 실행 시 null인 필드를 제외하여, DB 또는 @ColumnDefault에 설정된 기본값이 적용되도록 합니다.
@Table(name = "Users") // 데이터베이스의 "Users" 테이블과 매핑됩니다.
public class Users {
    /**
     * 사용자의 고유 식별자(PK)입니다.
     * 데이터베이스에서 자동으로 생성되는 번호(Auto Increment)를 사용합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Users_IDX")
    private Long usersIdx;

    /**
     * 로그인에 사용되는 아이디(이메일)입니다.
     * 최대 254자까지 저장 가능하며, 중복될 수 없습니다(Unique 제약 조건은 DB 레벨에서 관리됨을 가정).
     */
    @Column(name = "ID", nullable = false, length = 254)
    private String ID;

    /**
     * 암호화된 비밀번호입니다.
     * BCrypt 등의 알고리즘으로 해싱된 문자열이 저장됩니다.
     */
    @Column(name = "PW", nullable = false)
    private String PW;

    /**
     * 로그인 실패 횟수입니다.
     * 연속적인 로그인 실패 시 계정을 잠그는 보안 기능을 위해 사용됩니다.
     * 기본값은 0입니다.
     */
    @Column(name = "Failed_Login_Attempts", nullable = false)
    @ColumnDefault("0")
    private Integer failedLoginAttempts = 0;

    /**
     * 계정 잠금 만료 시간입니다.
     * 로그인 실패 허용 횟수 초과 시, 이 시간에 설정된 시각까지 로그인이 제한됩니다.
     * (NULL일 경우 잠금 상태가 아님)
     */
    @Column(name = "Locked_Until")
    private LocalDateTime lockedUntil;

    /**
     * 사용자의 권한(역할)입니다.
     * USERS(일반), SELLER(판매자), ADMIN(관리자) 등으로 구분됩니다.
     * 기본값은 'USERS'입니다.
     */
    @Enumerated(EnumType.STRING) // Enum 이름을 문자열 그대로 DB에 저장합니다.
    @Column(name = "Role", nullable = false, length = 20)
    @ColumnDefault("'Users'")
    private UsersRole role = UsersRole.USERS;

    /**
     * 사용자의 보유 잔액(포인트/예치금)입니다.
     * 금융 데이터이므로 정확한 연산을 위해 BigDecimal 타입을 사용합니다.
     */
    @Column(name = "Balance", nullable = false, precision = 19, scale = 2)
    @ColumnDefault("0")
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * 이용약관 동의 일시입니다.
     * 회원가입 시점에 기록되며 수정되지 않습니다.
     */
    @CreatedDate
    @Column(name = "agree_Terms_at", nullable = false, updatable = false)
    private LocalDateTime agreeTermsAt;

    /**
     * 개인정보 처리방침 동의 일시입니다.
     * 회원가입 시점에 기록되며 수정되지 않습니다.
     */
    @CreatedDate
    @Column(name = "agree_Privacy_at", nullable = false, updatable = false)
    private LocalDateTime agreePrivacyAt;

    /**
     * 마케팅 정보 수신 동의 일시입니다.
     * 선택 항목이므로 NULL일 수 있습니다.
     */
    @Column(name = "agree_Marketing_at")
    private LocalDateTime agreeMarketingAt;

    /**
     * 계정 생성 일시입니다.
     * JPA Auditing에 의해 자동으로 값이 주입됩니다.
     */
    @CreatedDate
    @Column(name = "Created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 계정 정보가 마지막으로 수정된 일시입니다.
     * JPA Auditing에 의해 변경 시 자동으로 갱신됩니다.
     */
    @LastModifiedDate
    @Column(name = "Updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 계정 상태(삭제 여부)를 나타내는 플래그입니다.
     * 0: 정상 (활성 상태)
     * 1: 탈퇴 (삭제됨)
     * 2: 미인증 (이메일 인증 대기 등)
     * 기본값은 2(미인증)로 설정되어 회원가입 직후 인증을 유도합니다.
     */
    @Column(name = "Del", nullable = false)
    @ColumnDefault("2")
    private int del = 2;

    /**
     * 회원가입 시 필수 정보를 초기화하는 빌더 생성자입니다.
     * 아이디, 비밀번호 및 필수 약관 동의 일시를 설정합니다.
     */
    @Builder
    public Users(String ID, String PW, LocalDateTime agreeTermsAt, LocalDateTime agreePrivacyAt, LocalDateTime agreeMarketingAt) {
        this.ID = ID;
        this.PW = PW;
        this.agreeTermsAt = agreeTermsAt;
        this.agreePrivacyAt = agreePrivacyAt;
        this.agreeMarketingAt = agreeMarketingAt;
    }

    /**
     * 이메일 인증이 완료되었을 때 호출되는 메서드입니다.
     * 계정 상태(Del)를 2(미인증)에서 0(정상)으로 변경합니다.
     */
    public void confirmVerification() {
        this.del = 0;
    }

    /**
     * 비밀번호를 임시 값("expire")으로 초기화하는 메서드입니다.
     * (주로 관리자 기능이나 특정 비상 상황에서 사용될 것으로 추정)
     * 로그인 실패 횟수도 함께 초기화합니다.
     */
    public void resetPW() {
        this.PW = "expire";
        this.failedLoginAttempts = 0;
    }

    /**
     * 비밀번호를 변경하는 메서드입니다.
     * 변경 시 로그인 실패 횟수도 0으로 초기화하여 잠금 상태를 방지합니다.
     *
     * @param PW 새로 설정할 암호화된 비밀번호
     */
    public void setPW(String PW) {
        this.PW = PW;
        this.failedLoginAttempts = 0;
    }

    /**
     * 로그인 실패 시 호출되는 메서드입니다.
     * 실패 횟수를 1 증가시킵니다.
     */
    public void loginFail() {
        this.failedLoginAttempts += 1;
    }

    /**
     * 계정을 15분간 잠그는 메서드입니다.
     * 로그인 실패 횟수가 허용치를 초과했을 때 호출됩니다.
     * 실패 횟수를 초기화하고, 잠금 만료 시간(현재 시간 + 15분)을 설정합니다.
     */
    public void lockFor15Minutes() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = LocalDateTime.now().plusMinutes(15);
    }

    /**
     * 로그인 성공 시 호출되는 메서드입니다.
     * 누적된 로그인 실패 횟수를 0으로 초기화합니다.
     */
    public void loginSuccess() {
        this.failedLoginAttempts = 0;
    }

    /**
     * 사용자의 권한을 변경하는 메서드입니다.
     * 예: 일반 유저 -> 판매자, 일반 유저 -> 관리자 등
     *
     * @param role 변경할 권한(UsersRole)
     */
    public void setRole(UsersRole role) {
        this.role = role;
    }

    /**
     * 관리자가 사용자의 계정을 특정 시간까지 강제로 정지시킬 때 사용하는 메서드입니다.
     *
     * @param lockedUntil 정지가 해제되는 일시
     */
    public void AdminLockUser(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    /**
     * 관리자가 사용자를 강제 탈퇴 처리할 때 사용하는 메서드입니다.
     * 계정 상태(Del)를 1(탈퇴)로 변경합니다.
     */
    public void AdminUserDel() {
        this.del = 1;
    }

    /**
     * 사용자가 스스로 회원 탈퇴를 요청할 때 사용하는 메서드입니다.
     * 계정 상태(Del)를 1(탈퇴)로 변경합니다.
     */
    public void del() {
        this.del = 1;
    }
}