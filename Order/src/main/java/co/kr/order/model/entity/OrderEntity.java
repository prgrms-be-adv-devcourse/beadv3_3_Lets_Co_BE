package co.kr.order.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Entity
@Table(name = "Orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Order_IDX")
    private Long id;

    @Column(name = "Order_Code")
    private String orderCode;

    @OneToMany(mappedBy = "order")
    private List<OrderItemEntity> orderItems;

    // user 외래키로 받기
    // address 외래키 받기
    // card 외래키 받기

    @Column(name = "Status")
    private String status;

    @Column(name = "Items_Account", precision = 19, scale = 2)
    private BigDecimal itemsAccount;

//    @Column(name = "Discount_Amount", precision = 19, scale = 2)
//    private BigDecimal discountAmount;

//    @Column(name = "Shipping_Fee", precision = 19, scale = 2)
//    private BigDecimal shippingFee;

    @Column(name = "Total_Amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "Del")
    private Boolean del;

    @Builder
    public OrderEntity(String orderCode, String status, BigDecimal itemsAccount, BigDecimal totalAmount) {

        this.orderCode = orderCode;
        this.status = status;
        this.itemsAccount = itemsAccount;
        this.totalAmount = totalAmount;
        this.del = false;
    }
}
