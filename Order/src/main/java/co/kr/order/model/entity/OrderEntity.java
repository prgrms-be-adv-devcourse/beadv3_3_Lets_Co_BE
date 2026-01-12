package co.kr.order.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Orders_IDX")
    private Long id;

    @OneToMany(mappedBy = "order")
    private List<OrderItemEntity> orderItems;

    @Column(name = "Users_IDX", nullable = false)
    private Long userIdx;

    @Column(name = "Address_IDX", nullable = false)
    private Long addressIdx;

    @Column(name = "Card_IDX", nullable = false)
    private Long cardIdx;


    @Column(name = "Orders_Code", nullable = false)
    private String orderCode;

    @Column(name = "Status", nullable = false)
    private String status;

    @Column(name = "Items_Amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal itemsAmount;

//    @Column(name = "Discount_Amount", precision = 19, scale = 2)
//    private BigDecimal discountAmount;

//    @Column(name = "Shipping_Fee", precision = 19, scale = 2)
//    private BigDecimal shippingFee;

    @Column(name = "Total_Amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "Del", nullable = false)
    private Boolean del;
}
