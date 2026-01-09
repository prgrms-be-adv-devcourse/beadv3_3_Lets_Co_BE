package co.kr.product.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
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
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Option_Group_IDX")
    private Long optionGroupIdx;

    @Column(name = "Products_IDX", nullable = false)
    private Long productsIdx;

    @Column(name = "Option_Code", nullable = false, length = 50)
    private String optionCode;

    @Column(name = "Option_Name", nullable = false, length = 50)
    private String optionName;

    @Column(name = "Sort_Orders", nullable = false)
    @ColumnDefault("0")
    private Integer sortOrders;

    @Column(name = "Option_Price", nullable = false, precision = 19, scale = 2)
    private BigDecimal optionPrice;

    @Column(name = "Option_Sale_Price", nullable = false, precision = 19, scale = 2)
    @ColumnDefault("0")
    private BigDecimal optionSalePrice;

    @Column(name = "Stock", nullable = false)
    @ColumnDefault("0")
    private Integer stock;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;

    @Column(name = "Created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private Boolean del;
}

