package co.kr.user.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
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

    @Column(name = "Default_Card", nullable = false)
    @ColumnDefault("0")
    private int defaultCard;

    @Column(name = "Card_Brand", nullable = false, length = 20)
    private String cardBrand;

    @Column(name = "Card_Name", nullable = false, length = 200)
    private String cardName;

    @Column(name = "Card_Token", nullable = false)
    private String cardToken;

    @Column(name = "Exp_Month", nullable = false)
    private int expMonth;

    @Column(name = "Exp_Year", nullable = false)
    private int expYear;

    @Column(name = "Del")
    @ColumnDefault("0")
    private int del;

    @Builder
    public UserCard(Long usersIdx, String cardCode, int defaultCard, String cardBrand, String cardName, String cardToken, int expMonth, int expYear) {
        this.usersIdx = usersIdx;
        this.cardCode = cardCode;
        this.defaultCard = defaultCard;
        this.cardBrand = cardBrand;
        this.cardName = cardName;
        this.cardToken = cardToken;
        this.expMonth = expMonth;
        this.expYear = expYear;
    }

    public void updateCard(int defaultCard, String cardBrand, String cardName, String cardToken, int expMonth, int expYear) {
        this.defaultCard = defaultCard;
        this.cardBrand = cardBrand;
        this.cardName = cardName;
        this.cardToken = cardToken;
        this.expMonth = expMonth;
        this.expYear = expYear;
    }

    public void del() {
        this.del = 1;
    }

}