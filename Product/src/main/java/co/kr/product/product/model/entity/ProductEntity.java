package co.kr.product.product.model.entity;

import co.kr.product.product.model.vo.ProductStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// createdAt,Updated_at을 null로 보낼 수 있도록 함
@DynamicInsert
@DynamicUpdate
@Table(name = "Products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Products_IDX")
    private Long productsIdx;

    @Column(name = "Seller_IDX", nullable = false)
    private Long sellerIdx;

    @Column(name = "Products_Code", nullable = false, length = 50, unique = true)
    private String productsCode;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20)
    private ProductStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Products_Category", nullable = false)
    private ProductCategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Products_IP", nullable = false)
    private ProductCategoryEntity ip;


    @Column(name = "View_Count", nullable = false)
    @ColumnDefault("0")
    private Long viewCount;

    @Column(name = "Created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private Boolean del = false;

    @Builder
    public ProductEntity(Long sellerIdx,
                         String productsCode,
                         String productsName,
                         String description,
                         BigDecimal price,
                         BigDecimal salePrice,
                         Integer stock,
                         ProductStatus status,
                         ProductCategoryEntity category,
                         ProductCategoryEntity ip) {

        this.sellerIdx = sellerIdx;
        this.productsCode = productsCode;
        this.productsName = productsName;
        this.description = description;
        this.price = price;
        this.salePrice = (salePrice == null ? BigDecimal.ZERO : salePrice);
        this.stock = (stock == null ? 0 : stock);
        this.status = status;
        this.category = category;
        this.ip = ip;
        this.viewCount = 0L;
        this.del = false;
    }

    public void update(String productsName,
                       String description,
                       BigDecimal price,
                       BigDecimal salePrice,
                       int stock,
                       ProductStatus status,
                       ProductCategoryEntity category,
                       ProductCategoryEntity ip) {
        this.productsName = productsName;
        this.description = description;
        this.price = price;
        this.salePrice = salePrice;
        this.stock = stock;
        this.status = status;
        this.category =category;
        this.ip = ip;

    }

    //softDelete
    public void delete(){
        this.del = true;
    }

    // 조회수 증가(상세 조회 시 사용)
    public void increaseViewCount() {
        this.viewCount = (this.viewCount == null ? 1L : this.viewCount + 1L);}


}


