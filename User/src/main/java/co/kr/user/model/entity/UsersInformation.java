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

    @Builder
    public UsersInformation(Long usersIdx, UsersInformationGender gender, String name, String phoneNumber, String birth, LocalDateTime agreeMarketingAt) {
        this.usersIdx = usersIdx;
        this.gender = gender;
        this.balance  = BigDecimal.valueOf(0.00);
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birth = birth;
        this.defaultAddress = null;
        this.defaultCard = null;
        this.agreeMarketingAt = agreeMarketingAt;
    }

    public void updatePrePW(String prePW) {
        this.prePW = prePW;
    }

    public void updateInformation(UsersInformationGender gender, String name, String phoneNumber, String birth) {
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
