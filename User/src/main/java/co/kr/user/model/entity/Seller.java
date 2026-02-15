package co.kr.user.model.entity;

import co.kr.user.model.vo.UserDel;
import co.kr.user.util.CryptoConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 판매자(Seller) 정보를 관리하는 Entity 클래스입니다.
 * 판매자명, 사업자 번호, 정산용 계좌 정보 등을 포함하며, 승인 대기 또는 활성 상태를 가집니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Seller")
public class Seller {
    /** 판매자 고유 식별자 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Seller_IDX")
    private Long sellerIdx;

    /** 해당 판매자 권한을 가진 사용자 식별자 */
    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    /** 상점명 또는 판매자명 (암호화 저장) */
    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Seller_Name", nullable = false, length = 512)
    private String sellerName;

    /** 사업자 등록 번호 (암호화 저장) */
    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Business_License", nullable = false, length = 512)
    private String businessLicense;

    /** 정산 계좌 은행명 (암호화 저장) */
    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Bank_Brand", nullable = false, length = 512)
    private String bankBrand;

    /** 예금주 성명 (암호화 저장) */
    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Bank_Name", nullable = false, length = 512)
    private String bankName;

    /** 암호화된 계좌 번호 또는 토큰 */
    @Column(name = "Bank_Token", nullable = false, length = 2048)
    private String bankToken;

    /** 판매자 등록 신청 일시 */
    @Column(name = "Created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 판매자 정보 최종 수정 일시 */
    @LastModifiedDate
    @Column(name = "Updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /** 판매자 상태 (PENDING: 승인대기, ACTIVE: 정상, DELETED: 폐점) */
    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private UserDel del;

    @Builder
    public Seller(Long usersIdx, String sellerName, String businessLicense, String bankBrand, String bankName, String bankToken) {
        if (usersIdx == null) throw new IllegalArgumentException("사용자 식별자는 필수입니다.");
        if (!StringUtils.hasText(sellerName)) throw new IllegalArgumentException("판매자 명은 필수입니다.");

        this.usersIdx = usersIdx;
        this.sellerName = sellerName;
        this.businessLicense = businessLicense;
        this.bankBrand = bankBrand;
        this.bankName = bankName;
        this.bankToken = bankToken;
        this.del = UserDel.PENDING; // 신청 직후에는 대기 상태
    }

    /** 판매자 정보를 수정합니다. (은행 정보 및 상점명) */
    public void updateSeller(String sellerName, String bankBrand, String bankName, String encodedBankToken) {
        if (StringUtils.hasText(sellerName)) {
            this.sellerName = sellerName.trim();
        }
        if (StringUtils.hasText(bankBrand)) {
            this.bankBrand = bankBrand.trim();
        }
        if (StringUtils.hasText(bankName)) {
            this.bankName = bankName.trim();
        }
        if (StringUtils.hasText(encodedBankToken)) {
            this.bankToken = encodedBankToken;
        }
    }

    /** 관리자 승인 또는 인증 완료 시 판매자 상태를 활성화합니다. */
    public void activateSeller() {
        this.del = UserDel.ACTIVE;
    }

    /** 판매자 자격을 박탈하거나 폐점 처리(논리적 삭제)합니다. */
    public void deleteSeller() {
        this.del = UserDel.DELETED;
    }
}