package co.kr.order.model.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "Cart")
public class CartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Cart_Item_IDX")
    private Long id;

    @Column(name = "User_IDX", nullable = false)
    private Long userId;

    @Column(name = "Product_IDX", nullable = false)
    private Long productId;

    @Column(name = "Option_Group_IDX", nullable = false)
    private Long optionId;

    @Column(name = "Price", precision = 19, scale = 2, nullable = false)
    private BigDecimal price;

//    @Column(name = "Sale_Price", precision = 19, scale = 2)
//    private BigDecimal salePrice;

    @Column(name = "Count", nullable = false)
    private Integer count;

    @Column(name = "Del", nullable = false)
    Boolean del;
}
