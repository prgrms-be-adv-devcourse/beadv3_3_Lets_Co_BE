package co.kr.user.model.entity;

import co.kr.user.model.vo.UsersInformationGender;
import co.kr.user.util.CryptoConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Mail", nullable = false)
    private String mail;

    @Enumerated(EnumType.STRING)
    @Column(name = "Gender", nullable = false)
    private UsersInformationGender gender;

    @Column(name = "Balance",nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Name", nullable = false, length = 512)
    private String name;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Phone_Number", nullable = false, length = 512)
    private String phoneNumber;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Birth", nullable = false, length = 512)
    private String birth;

    @Column(name = "Default_Address")
    private Long defaultAddress;

    @Column(name = "Default_Card")
    private Long defaultCard;

    @Column(name = "agree_Marketing_at")
    private LocalDateTime agreeMarketingAt;

    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private int del;

    @Builder
    public UsersInformation(Long usersIdx, String mail, UsersInformationGender gender, String name, String phoneNumber, String birth, LocalDateTime agreeMarketingAt) {
        this.usersIdx = usersIdx;
        this.mail = mail;
        this.gender = gender;
        this.balance  = BigDecimal.valueOf(0.00);
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birth = birth;
        this.defaultAddress = null;
        this.defaultCard = null;
        this.agreeMarketingAt = agreeMarketingAt;
        this.del = 2;
    }

    public void checkAccountStatus() {
        if (this.del == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        if (this.del == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }
    }

    public void activateInformation() {
        this.del = 0;
    }

    public void deleteInformation(String mail) {
        this.mail = mail;
        this.del = 1;
    }

    public void updatePrePW(String prePW) {
        this.prePW = prePW;
    }

    public void updateInformation(String mail, UsersInformationGender gender, String name, String phoneNumber, String birth) {
        this.mail = mail;
        this.gender = gender;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birth = birth;
    }

    public void updateBalance(BigDecimal balance) {
        this.balance = balance;
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
}
