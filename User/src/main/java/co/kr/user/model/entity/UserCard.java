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

/**
 * 사용자의 결제 카드 정보를 관리하는 Entity 클래스입니다.
 * 사용자는 여러 개의 카드를 등록할 수 있으며, 실제 결제 시 사용되는 토큰 정보를 포함합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Users_Card")
public class UserCard {
    /** 카드 고유 식별자 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Card_IDX", nullable = false)
    private Long cardIdx;

    /** 카드 소유 사용자의 식별자 */
    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    /** 외부 식별용 카드 고유 코드 (UUID) */
    @Column(name = "Card_Code", nullable = false, length = 100)
    private String cardCode;

    /** 카드 브랜드 (예: VISA, MASTER 등 / 암호화 저장) */
    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Card_Brand", nullable = false)
    private String cardBrand;

    /** 카드 별칭 또는 이름 (암호화 저장) */
    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Card_Name", nullable = false)
    private String cardName;

    /** 결제 게이트웨이(PG)로부터 발급받은 카드 빌링 토큰 (암호화 저장) */
    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Card_Token", nullable = false, length = 512)
    private String cardToken;

    /** 카드 유효기간 - 월 */
    @Column(name = "Exp_Month", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private int expMonth;

    /** 카드 유효기간 - 년 */
    @Column(name = "Exp_Year", nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private int expYear;

    /** 카드 삭제 여부 상태 */
    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private UserDel del;

    @Builder
    public UserCard(Long usersIdx, String cardBrand, String cardName, String cardToken, int expMonth, int expYear) {
        if (usersIdx == null) throw new IllegalArgumentException("사용자 식별자는 필수입니다.");
        if (expMonth < 1 || expMonth > 12) throw new IllegalArgumentException("유효하지 않은 만료 월입니다.");

        this.usersIdx = usersIdx;
        this.cardCode = UUID.randomUUID().toString(); // 카드 등록 시 고유 코드 자동 생성
        this.cardBrand = cardBrand;
        this.cardName = cardName;
        this.cardToken = cardToken;
        this.expMonth = expMonth;
        this.expYear = expYear;
        this.del = UserDel.ACTIVE;
    }

    /** 등록된 카드 정보를 업데이트합니다. */
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

    /** 카드를 논리적으로 삭제 처리합니다. */
    public void deleteCard() {
        this.del = UserDel.DELETED;
    }
}