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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Users_Information")
public class UsersInformation {
    @Id
    @Column(name = "Users_IDX")
    private Long usersIdx;

    @Column(name = "Pre_PW")
    private String prePW;

    @Convert(converter = CryptoConverter.DeterministicConverter.class)
    @Column(name = "Mail", nullable = false)
    private String mail;

    @Enumerated(EnumType.STRING)
    @Column(name = "Gender", nullable = false)
    private UsersInformationGender gender;

    @Column(name = "Balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Name", nullable = false, length = 512)
    private String name;

    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Phone_Number", nullable = false, length = 512)
    private String phoneNumber;

    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Birth", nullable = false, length = 512)
    private String birth;

    @Column(name = "Default_Address")
    private Long defaultAddress;

    @Column(name = "Default_Card")
    private Long defaultCard;

    @Column(name = "agree_Marketing_at")
    private LocalDateTime agreeMarketingAt;

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

    public void checkAccountStatus() {
        if (this.del == UserDel.DELETED) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        if (this.del == UserDel.PENDING) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }
    }

    public void activateInformation() {
        this.del = UserDel.ACTIVE;
    }

    public void deleteInformation(String mail) {
        this.mail = mail;
        this.del = UserDel.DELETED;
    }

    public void updatePrePW(String prePW) {
        this.prePW = prePW;
    }

    public void updateProfile(String mail, UsersInformationGender gender, String name, String phoneNumber, String birth) {
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
    }

    public void chargeBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        this.balance = this.balance.add(amount);
    }

    public void pay(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("잔액이 부족하여 결제할 수 없습니다.");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void updateDefaultAddress(Long defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public void updateDefaultCard(Long defaultCard) {
        this.defaultCard = defaultCard;
    }

    public void updateAgreeMarketingAt(LocalDateTime agreeMarketingAt) {
        this.agreeMarketingAt = agreeMarketingAt;
    }

    public void validateNewPassword(String newEncodedPw, BiFunction<String, String, Boolean> passwordChecker) {
        if (StringUtils.hasText(this.prePW) && passwordChecker.apply(newEncodedPw, this.prePW)) {
            throw new IllegalArgumentException("과거에 사용했던 비밀번호는 다시 사용할 수 없습니다.");
        }
    }
}