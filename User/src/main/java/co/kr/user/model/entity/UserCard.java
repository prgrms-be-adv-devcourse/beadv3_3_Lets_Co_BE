package co.kr.user.model.entity;

import co.kr.user.model.vo.UserDel;
import co.kr.user.util.CryptoConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.util.StringUtils;

import java.util.UUID;

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

    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Card_Brand", nullable = false)
    private String cardBrand;

    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Card_Name", nullable = false)
    private String cardName;

    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Card_Token", nullable = false, length = 512)
    private String cardToken;

    @Column(name = "Exp_Month", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private int expMonth;

    @Column(name = "Exp_Year", nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private int expYear;

    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private UserDel del;

    @Builder
    public UserCard(Long usersIdx, String cardBrand, String cardName, String cardToken, int expMonth, int expYear) {
        if (usersIdx == null) throw new IllegalArgumentException("사용자 식별자는 필수입니다.");
        if (expMonth < 1 || expMonth > 12) throw new IllegalArgumentException("유효하지 않은 만료 월입니다.");

        this.usersIdx = usersIdx;
        this.cardCode = UUID.randomUUID().toString();
        this.cardBrand = cardBrand;
        this.cardName = cardName;
        this.cardToken = cardToken;
        this.expMonth = expMonth;
        this.expYear = expYear;
        this.del = UserDel.ACTIVE;
    }

    public void updateCard(String cardBrand, String cardName, String cardToken, int expMonth, int expYear) {
        if (StringUtils.hasText(cardBrand)) {
            this.cardBrand = cardBrand.trim();
        }
        if (StringUtils.hasText(cardName)) {
            this.cardName = cardName.trim();
        }
        if (StringUtils.hasText(cardToken)) {
            this.cardToken = cardToken;
        }
        if (expMonth >= 1 && expMonth <= 12) {
            this.expMonth = expMonth;
        }
        if (expYear > 0) {
            this.expYear = expYear;
        }
    }

    public void deleteCard() {
        this.del = UserDel.DELETED;
    }
}