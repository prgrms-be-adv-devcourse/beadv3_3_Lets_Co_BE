package co.kr.product.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
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
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Image_IDX")
    private Long imageIdx;

    @Column(name = "Products_IDX", nullable = false)
    private Long productsIdx;

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
}
