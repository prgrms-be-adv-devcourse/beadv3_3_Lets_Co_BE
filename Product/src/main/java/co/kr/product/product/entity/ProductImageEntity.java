package co.kr.product.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Product_Images")
public class ProductImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Image_IDX")
    private Long imageIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Products_IDX", nullable = false)
    private ProductEntity product;

    @Column(name = "Url", nullable = false, length = 255)
    private String url;

    @Column(name = "Sort_Orders", nullable = false)
    @ColumnDefault("0")
    private Integer sortOrders;

    @Column(name = "Is_Thumbnail", nullable = false)
    @ColumnDefault("0")
    private Boolean isThumbnail;

    @Column(name = "Created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private Boolean del;

    @Builder
    public ProductImageEntity(ProductEntity product, String url, Integer sortOrders, Boolean isThumbnail) {
        this.product = product;
        this.url = url;
        this.sortOrders = sortOrders;
        this.isThumbnail = isThumbnail;
        this.del = false;
    }

    public void delete() {this.del = true;}
}
