package co.kr.user.model.entity;

import co.kr.user.util.CryptoConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Seller")
public class Seller {

    @Id
    @Column(name = "Seller_IDX", nullable = false)
    private Long sellerIdx;

    @Convert(converter = CryptoConverter.class) // 추가
    @Column(name = "Seller_Name", nullable = false, length = 512)
    private String sellerName;

    @Convert(converter = CryptoConverter.class) // 추가
    @Column(name = "Business_License", nullable = false, length = 512)
    private String businessLicense;

    @Convert(converter = CryptoConverter.class) // 추가
    @Column(name = "Bank_Brand", nullable = false, length = 512)
    private String bankBrand;

    @Convert(converter = CryptoConverter.class) // 추가
    @Column(name = "Bank_Name", nullable = false, length = 512)
    private String bankName;

    @Convert(converter = CryptoConverter.class) // 추가
    @Column(name = "Bank_Token", nullable = false, length = 2048)
    private String bankToken;

    @Column(name = "Created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "Updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private int del;

    @Builder
    public Seller(Long sellerIdx, String sellerName, String businessLicense, String bankBrand, String bankName, String bankToken) {
        this.sellerIdx = sellerIdx;
        this.sellerName = sellerName;
        this.businessLicense = businessLicense;
        this.bankBrand = bankBrand;
        this.bankName = bankName;
        this.bankToken = bankToken;
        this.createdAt = null;
        this.updatedAt = null;
        this.del = 2;
    }

    public void updateSeller(String bankBrand, String bankName, String bankToken) {
        this.bankBrand = bankBrand;
        this.bankName = bankName;
        this.bankToken = bankToken;
    }

    public void activateSeller() {
        this.del = 0;
    }

    public void deleteSeller() {
        this.del = 1;
    }
}