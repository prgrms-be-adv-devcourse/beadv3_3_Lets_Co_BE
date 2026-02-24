package co.kr.user.model.entity;

import co.kr.user.model.vo.UserDel;
import co.kr.user.model.vo.UsersInformationGender;
import co.kr.user.util.CryptoConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.BiFunction;

/**
 * 사용자의 민감한 개인정보 및 자산(잔액) 정보를 관리하는 Entity 클래스입니다.
 * 별도의 테이블로 분리되어 보안 강화 및 암호화 처리가 집중되어 있습니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Users_Information")
public class UsersInformation {
    /** 사용자 고유 식별자 (Users 엔티티와 공유되는 PK) */
    @Id
    @Column(name = "Users_IDX")
    private Long usersIdx;

    /** 이전 비밀번호 (비밀번호 변경 시 이력 체크용) */
    @Column(name = "Pre_PW")
    private String prePW;

    /** 이메일 주소 (결정적 암호화 적용으로 검색 가능) */
    @Convert(converter = CryptoConverter.DeterministicConverter.class)
    @Column(name = "Mail", nullable = false)
    private String mail;

    /** 성별 */
    @Enumerated(EnumType.STRING)
    @Column(name = "Gender", nullable = false)
    private UsersInformationGender gender;

    /** 현재 잔액 (포인트/머니) */
    @Column(name = "Balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    /** 사용자 이름 (비결정적 암호화 적용 - 검색 불가, 조회 전용) */
    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Name", nullable = false, length = 512)
    private String name;

    /** 휴대폰 번호 (비결정적 암호화 적용) */
    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Phone_Number", nullable = false, length = 512)
    private String phoneNumber;

    /** 생년월일 (비결정적 암호화 적용) */
    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Birth", nullable = false, length = 512)
    private String birth;

    /** 기본 배송지 식별자 (UsersAddress의 PK) */
    @Column(name = "Default_Address")
    private Long defaultAddress;

    /** 기본 결제 카드 식별자 (UserCard의 PK) */
    @Column(name = "Default_Card")
    private Long defaultCard;

    /** 마케팅 수신 동의 일시 */
    @Column(name = "agree_Marketing_at")
    private LocalDateTime agreeMarketingAt;

    /** 삭제/활성 상태 */
    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private UserDel del;

    @Builder
    public UsersInformation(Long usersIdx, String mail, UsersInformationGender gender, String name, String phoneNumber, String birth, LocalDateTime agreeMarketingAt) {
        this.usersIdx = usersIdx;
        this.mail = mail;
        this.gender = gender;
        this.balance = BigDecimal.valueOf(0.00);
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birth = birth;
        this.defaultAddress = null;
        this.defaultCard = null;
        this.agreeMarketingAt = agreeMarketingAt;
        this.del = UserDel.PENDING;
    }

    /** 상세 정보의 유효 상태를 체크합니다. */
    public void checkAccountStatus() {
        if (this.del == UserDel.DELETED) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        if (this.del == UserDel.PENDING) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }
    }

    /** 정보를 활성(ACTIVE) 상태로 변경합니다. */
    public void activateInformation() {
        this.del = UserDel.ACTIVE;
    }

    /** 개인정보를 논리적으로 삭제 처리합니다. */
    public void deleteInformation(String mail) {
        this.mail = mail;
        this.del = UserDel.DELETED;
    }

    /** 비밀번호 변경 이력을 업데이트합니다. */
    public void updatePrePW(String prePW) {
        this.prePW = prePW;
    }

    /** 프로필 정보를 수정합니다. (Dirty Checking 활용) */
    public void updateProfile(String mail, UsersInformationGender gender, String name, String phoneNumber, String birth, LocalDateTime agreeMarketingAt) {
        if (StringUtils.hasText(mail)) {
            this.mail = mail;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (StringUtils.hasText(name)) {
            this.name = name.trim();
        }
        if (StringUtils.hasText(phoneNumber)) {
            this.phoneNumber = phoneNumber;
        }
        if (StringUtils.hasText(birth)) {
            this.birth = birth;
        }
        if (agreeMarketingAt != null) {
            this.agreeMarketingAt = agreeMarketingAt;
        }
    }

    /** 잔액을 충전합니다. */
    public void chargeBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        this.balance = this.balance.add(amount);
    }

    /** 금액을 결제합니다. 잔액 부족 시 예외가 발생합니다. */
    public void pay(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("잔액이 부족하여 결제할 수 없습니다.");
        }
        this.balance = this.balance.subtract(amount);
    }

    /** 기본 배송지를 설정합니다. */
    public void updateDefaultAddress(Long defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    /** 기본 카드를 설정합니다. */
    public void updateDefaultCard(Long defaultCard) {
        this.defaultCard = defaultCard;
    }

    /** 마케팅 수신 동의 여부를 업데이트합니다. */
    public void updateAgreeMarketingAt(LocalDateTime agreeMarketingAt) {
        this.agreeMarketingAt = agreeMarketingAt;
    }

    /** 새로운 비밀번호가 이전 비밀번호와 겹치는지 검증합니다. */
    public void validateNewPassword(String newEncodedPw, BiFunction<String, String, Boolean> passwordChecker) {
        if (StringUtils.hasText(this.prePW) && passwordChecker.apply(newEncodedPw, this.prePW)) {
            throw new IllegalArgumentException("과거에 사용했던 비밀번호는 다시 사용할 수 없습니다.");
        }
    }
}