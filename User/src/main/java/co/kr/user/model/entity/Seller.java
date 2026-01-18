package co.kr.user.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
@Table(name = "Seller")
public class Seller {
    @Id
    @Column(name = "Seller_IDX", nullable = false)
    private Long sellerIdx;

    @Column(name = "Business_License", nullable = false, length = 20)
    private String businessLicense;

    @Column(name = "Bank_Brand", nullable = false, length = 20)
    private String bankBrand;

    @Column(name = "Bank_Name", nullable = false, length = 200)
    private String bankName;

    @Column(name = "Bank_Token", nullable = false)
    private String bankToken;

    @CreatedDate
    @Column(name = "Created_at", nullable = false, updatable = false)
    private String createdAt;

    @CreatedDate
    @Column(name = "Updated_at", nullable = false, updatable = false)
    private String updatedAt;

    @Column(name = "Del")
    @ColumnDefault("2")
    private int del;

    @Builder
    public Seller(Long sellerIdx, String businessLicense, String bankBrand, String bankName, String bankToken) {
        this.sellerIdx = sellerIdx;
        this.businessLicense = businessLicense;
        this.bankBrand = bankBrand;
        this.bankName = bankName;
        this.bankToken = bankToken;
    }

    public void confirmVerification() {
        this.del = 0;
    }
}