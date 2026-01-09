package co.kr.product.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Products") // DB 생성 SQL 기준
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Products_IDX")
    private Long productsIdx;

    // DB 컬럼: Seller_IDX (User_IDX 아님)
    @Column(name = "Seller_IDX", nullable = false)
    private Long sellerIdx;

    // DB 컬럼: Products_Code
    @Column(name = "Products_Code", nullable = false, length = 50, unique = true)
    private String productsCode;

    // DB 컬럼: Products_Name
    @Column(name = "Products_Name", nullable = false, length = 200)
    private String productsName;

    @Column(name = "Description", columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(name = "Price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "Sale_Price", nullable = false, precision = 19, scale = 2)
    @ColumnDefault("0")
    private BigDecimal salePrice;

    @Column(name = "Stock", nullable = false)
    @ColumnDefault("0")
    private Integer stock;

    @Column(name = "Status", length = 20)
    private String status;

    @Column(name = "View_Count", nullable = false)
    @ColumnDefault("0")
    private Long viewCount;

    @Column(name = "Created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // DB는 TINYINT(1), JPA에서 Boolean 매핑
    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private Boolean del;

    @Builder
    public Product(Long sellerIdx,
                   String productsCode,
                   String productsName,
                   String description,
                   BigDecimal price,
                   BigDecimal salePrice,
                   Integer stock,
                   String status) {

        this.sellerIdx = sellerIdx;
        this.productsCode = productsCode;
        this.productsName = productsName;
        this.description = description;
        this.price = price;
        this.salePrice = (salePrice == null ? BigDecimal.ZERO : salePrice);
        this.stock = (stock == null ? 0 : stock);
        this.status = status;
        this.viewCount = 0L;
        this.del = false;
    }

    // soft delete 편의 메서드(선택)
    public void softDelete() {
        this.del = true;
    }

    // 조회수 증가(상세 조회 시 사용)
    public void increaseViewCount() {
        this.viewCount = (this.viewCount == null ? 1L : this.viewCount + 1L);
    }
}


