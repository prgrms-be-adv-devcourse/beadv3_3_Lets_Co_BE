package co.kr.order.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "Orders_Item")
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Orders_Item_IDX")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Orders_IDX", nullable = false)
    private OrderEntity order;

    @Column(name = "Products_IDX", nullable = false)
    private Long productIdx;

    @Column(name = "Option_Group_IDX", nullable = false)
    private Long optionIdx;

    @Column(name = "Products_Name")
    private String productName;

    @Column(name = "Option_Name")
    private String optionName;

    @Column(name = "Price", precision = 19, scale = 2, nullable = false)
    private BigDecimal price;

//    @Column(name = "Sale_Price", precision = 19, scale = 2)
//    private BigDecimal salePrice;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "Del", nullable = false)
    private Boolean del;

    @Builder
    public OrderItemEntity(OrderEntity order, Long productIdx, Long optionIdx,
                           String productName, String optionName, BigDecimal price,
                           Integer quantity, Boolean del) {
        this.order = order;
        this.productIdx = productIdx;
        this.optionIdx = optionIdx;
        this.productName = productName;
        this.optionName = optionName;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.quantity = quantity != null ? quantity : 0;
        this.del = del != null ? del : false;
    }
}