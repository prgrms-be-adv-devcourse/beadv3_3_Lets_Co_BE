package co.kr.user.model.entity;

import co.kr.user.util.CryptoConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Users_Card")
public class UserCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Card_IDX", nullable = false)
    private Long cardIdx;

    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    @Column(name = "Card_Code", nullable = false, length = 100)
    private String cardCode;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Card_Brand", nullable = false)
    private String cardBrand;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Card_Name", nullable = false)
    private String cardName;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Card_Token", nullable = false, length = 512)
    private String cardToken;

    @Column(name = "Exp_Month", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private int expMonth;

    @Column(name = "Exp_Year", nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private int expYear;

    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private int del;

    @Builder
    public UserCard(Long usersIdx, String cardCode, String cardBrand, String cardName, String cardToken, int expMonth, int expYear) {
        this.usersIdx = usersIdx;
        this.cardCode = cardCode;
        this.cardBrand = cardBrand;
        this.cardName = cardName;
        this.cardToken = cardToken;
        this.expMonth = expMonth;
        this.expYear = expYear;
    }

    public void updateCard(String cardBrand, String cardName, String cardToken, int expMonth, int expYear) {
        this.cardBrand = cardBrand;
        this.cardName = cardName;
        this.cardToken = cardToken;
        this.expMonth = expMonth;
        this.expYear = expYear;
    }

    public void deleteCard() {
        this.del = 1;
    }
}