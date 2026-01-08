package co.kr.order.model.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
public class OrderItemEntity {

    @Id
    @Column(name = "Order_Item_IDX")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Order_IDX")
    private OrderEntity order;

//    @Column(name = "Product_IDX")
//    @Column(name = "Option_Group_IDX")

    @Column(name = "Price", precision = 19, scale = 2)
    private BigDecimal price;

//    @Column(name = "Sale_Price", precision = 19, scale = 2)
//    private BigDecimal salePrice;

    @Column(name = "Count")
    private Integer count;

    @Column(name = "Del")
    private Boolean del;


}