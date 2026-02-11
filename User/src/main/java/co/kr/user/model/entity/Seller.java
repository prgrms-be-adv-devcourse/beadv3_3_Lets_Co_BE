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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Seller")
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Seller_IDX")
    private Long sellerIdx;

    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Seller_Name", nullable = false, length = 512)
    private String sellerName;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Business_License", nullable = false, length = 512)
    private String businessLicense;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Bank_Brand", nullable = false, length = 512)
    private String bankBrand;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Bank_Name", nullable = false, length = 512)
    private String bankName;

    @Column(name = "Bank_Token", nullable = false, length = 2048)
    private String bankToken;

    @Column(name = "Created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "Updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

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
        this.del = UserDel.PENDING;
    }

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

    public void activateSeller() {
        this.del = UserDel.ACTIVE;
    }

    public void deleteSeller() {
        this.del = UserDel.DELETED;
    }
}