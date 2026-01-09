package co.kr.product.seller.entity;

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
@Table(name = "Product_Option")
public class OptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Option_Group_IDX")
    private Long optionGroupIdx;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Products_IDX", nullable = false)
    private ProductEntity product;

    @Column(name = "Option_Code", nullable = false, length = 50)
    private String code;

    @Column(name = "Option_Name", nullable = false, length = 50)
    private String name;

    @Column(name = "Sort_Orders", nullable = false)
    @ColumnDefault("0")
    private Integer sortOrder = 0;

    @Column(name = "Option_Price", precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "Option_Sale_Price", precision = 19, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "Stock")
    private Integer stock;

    @Column(name = "Status", length = 20)
    private String status;

    @Column(name = "Created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private Boolean del = false;


    @Builder
    public OptionEntity(ProductEntity product, String code, String name, Integer sortOrder,
                         BigDecimal price, BigDecimal salePrice, Integer stock, String status) {
        this.product = product;
        this.code = code;
        this.name = name;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.price = price;
        this.salePrice = salePrice;
        this.stock = stock;
    }

    public void update( String name,
                   int sortOrder,
                   BigDecimal price,
                   BigDecimal salePrice,
                   int stock,
                        String status){

        this.name = name;
        this.sortOrder =sortOrder;
        this.price = price;
        this.salePrice =salePrice;
        this.stock = stock;
        this.status = status;
    }

    public void delete(){
        this.del = true;
    }


}