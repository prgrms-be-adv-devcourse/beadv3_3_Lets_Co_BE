package co.kr.product.seller.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Products_IDX")
    private Long productsIdx;

    @Column(name = "Seller_IDX", nullable = false)
    private Long sellerIdx;

    @Column(name = "Products_Code", nullable = false, length = 36, unique = true)
    private String code;

    @Column(name = "Products_Name", nullable = false, length = 200)
    private String name;

    @Column(name = "Description", columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(name = "Price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "Sale_Price", precision = 19, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "Stock", nullable = false)
    @ColumnDefault("0") // DB Default 값 명시
    private Integer stock = 0;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;

    @Column(name = "View_Count", nullable = false)
    @ColumnDefault("0")
    private Long viewCount = 0L;


    @Column(name = "Created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @Column(name = "Updated_at", nullable = false, updatable = false)
    private LocalDateTime updatedAt;


    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private Boolean del = false;

    @Builder
    public ProductEntity(Long sellerIdx, String code, String name,String description, BigDecimal price, BigDecimal salePrice, Integer stock, String status) {
        this.sellerIdx = sellerIdx;
        this.code = code;
        this.name = name;
        this.description=  description;
        this.price = price;
        this.salePrice = salePrice;
        this.stock = stock;
    }

    public void update(String name, String description, BigDecimal price, BigDecimal salePrice, int stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.salePrice = salePrice;
        this.stock = stock;

    }

    public void delete(){
        this.del = true;
    }
}