package co.kr.order.model.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "Orders_Item")
public class OrderItemEntity {

    @Id
    @Column(name = "Orders_Item_IDX")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Orders_IDX", nullable = false)
    private OrderEntity orderIdx;

    @Column(name = "Products_IDX", nullable = false)
    private Long productIdx;

    @Column(name = "Option_Group_IDX", nullable = false)
    private Long optionIdx;

//    @Column(name = "Products_Name")
//    private String productsName;

    @Column(name = "Option_Name")
    private String optionName;

    @Column(name = "Price", precision = 19, scale = 2, nullable = false)
    private BigDecimal price;

//    @Column(name = "Sale_Price", precision = 19, scale = 2)
//    private BigDecimal salePrice;

    @Column(name = "Count", nullable = false)
    private Integer count;

    @Column(name = "Del", nullable = false)
    private Boolean del;
}