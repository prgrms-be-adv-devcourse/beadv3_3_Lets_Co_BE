package co.kr.user.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 사용자의 결제 수단(신용/체크카드) 정보를 관리하는 엔티티 클래스입니다.
 * 실제 카드 번호를 직접 저장하는 대신, PG사로부터 발급받은 빌링키(Token)나 암호화된 정보를 저장하여 보안성을 확보합니다.
 * 등록된 카드는 상품 구매 시 간편 결제 수단으로 사용될 수 있습니다.
 */
@Entity // JPA 엔티티임을 명시합니다.
@Getter // 모든 필드의 Getter 메서드를 자동 생성합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자의 접근을 PROTECTED로 제한하여 무분별한 생성을 방지합니다.
@EntityListeners(AuditingEntityListener.class) // 엔티티의 변경 감지(Auditing)를 위한 리스너를 등록합니다.
@DynamicInsert // INSERT 시 null인 필드를 제외하여 DB의 Default 값이 적용되도록 합니다.
@Table(name = "Users_Card") // 데이터베이스의 "Users_Card" 테이블과 매핑됩니다.
public class UserCard {
    /**
     * 카드 정보의 고유 식별자(PK)입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Card_IDX", nullable = false)
    private Long cardIdx;

    /**
     * 카드를 소유한 사용자의 식별자(FK)입니다.
     * Users 테이블의 PK를 참조합니다.
     */
    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    /**
     * 카드 관리를 위한 고유 코드입니다.
     * 외부 API 노출 시 PK 대신 사용하여 보안성을 높입니다 (UUID 등 사용).
     */
    @Column(name = "Card_Code", nullable = false, length = 100)
    private String cardCode;

    /**
     * 기본 결제 수단 여부입니다.
     * 0: 일반 카드
     * 1: 기본 카드 (결제 시 우선 선택됨)
     * 기본값은 0입니다.
     */
    @Column(name = "Default_Card", nullable = false)
    @ColumnDefault("0")
    private int defaultCard;

    /**
     * 카드사 이름 또는 브랜드입니다. (예: Shinhan, Visa, MasterCard)
     * 개인정보 보호를 위해 암호화되어 저장될 수 있습니다.
     */
    @Column(name = "Card_Brand", nullable = false, length = 200)
    private String cardBrand;

    /**
     * 카드의 별칭 또는 카드에 표기된 이름입니다.
     * 개인정보 보호를 위해 암호화되어 저장될 수 있습니다.
     */
    @Column(name = "Card_Name", nullable = false, length = 200)
    private String cardName;

    /**
     * 결제 승인을 위한 카드 토큰(Billing Key)입니다.
     * 카드 번호(PAN)를 직접 저장하지 않고, PG사에서 발급한 토큰을 저장하여 정기 결제나 간편 결제에 사용합니다.
     * 매우 중요한 정보이므로 반드시 암호화되어 저장되어야 합니다.
     */
    @Column(name = "Card_Token", nullable = false)
    private String cardToken;

    /**
     * 카드의 유효기간(월)입니다.
     */
    @Column(name = "Exp_Month", nullable = false)
    private int expMonth;

    /**
     * 카드의 유효기간(년)입니다.
     */
    @Column(name = "Exp_Year", nullable = false)
    private int expYear;

    /**
     * 카드 삭제 여부를 나타내는 플래그입니다.
     * 0: 정상 사용 가능
     * 1: 삭제됨 (Soft Delete)
     */
    @Column(name = "Del")
    @ColumnDefault("0")
    private int del;

    /**
     * 카드 객체 생성을 위한 빌더입니다.
     *
     * @param usersIdx 사용자 식별자
     * @param cardCode 카드 고유 코드
     * @param defaultCard 기본 카드 여부
     * @param cardBrand 카드사 정보
     * @param cardName 카드 별칭
     * @param cardToken 결제 토큰
     * @param expMonth 유효기간(월)
     * @param expYear 유효기간(년)
     */
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

    /**
     * 카드 정보를 수정하는 메서드입니다.
     * 유효기간 연장이나 카드 별칭 수정, 기본 카드 설정 변경 등에 사용됩니다.
     *
     * @param defaultCard 기본 카드 설정 여부
     * @param cardBrand 변경할 카드사
     * @param cardName 변경할 카드 별칭
     * @param cardToken 변경할 카드 토큰 (재발급 시)
     * @param expMonth 변경할 유효기간(월)
     * @param expYear 변경할 유효기간(년)
     */
    public void updateCard(int defaultCard, String cardBrand, String cardName, String cardToken, int expMonth, int expYear) {
        this.defaultCard = defaultCard;
        this.cardBrand = cardBrand;
        this.cardName = cardName;
        this.cardToken = cardToken;
        this.expMonth = expMonth;
        this.expYear = expYear;
    }

    /**
     * 카드를 삭제 처리하는 메서드입니다.
     * 실제 DB 레코드를 지우지 않고, Del 플래그를 1로 설정하여 비활성화(Soft Delete)합니다.
     */
    public void del() {
        this.del = 1;
    }
}