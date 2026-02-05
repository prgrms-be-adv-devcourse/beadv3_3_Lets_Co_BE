package co.kr.order.model.entity;

import co.kr.order.model.vo.OrderStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "Orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Orders_IDX")
    private Long id;

    @OneToMany(mappedBy = "order")
    private List<OrderItemEntity> orderItems = new ArrayList<>(); // null 방지 초기화

    @Column(name = "Users_IDX", nullable = false)
    private Long userIdx;

    @Column(name = "Address_IDX", nullable = true)
    private Long addressIdx;

    @Column(name = "Card_IDX", nullable = true)
    private Long cardIdx;

    @Column(name = "Orders_Code", nullable = false)
    private String orderCode;

    @Column(name = "Status", nullable = false)
    private OrderStatus status;

    @Column(name = "Items_Amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal itemsAmount;

//    @Column(name = "Discount_Amount", precision = 19, scale = 2)
//    private BigDecimal discountAmount;

//    @Column(name = "Shipping_Fee", precision = 19, scale = 2)
//    private BigDecimal shippingFee;

    @Column(name = "Total_Amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private Boolean del;

    @Builder
    public OrderEntity(Long userIdx, Long addressIdx, Long cardIdx, String orderCode,
                       OrderStatus status, BigDecimal itemsAmount, BigDecimal totalAmount, Boolean del) {
        this.userIdx = userIdx;
        this.addressIdx = addressIdx;
        this.cardIdx = cardIdx;
        this.orderCode = orderCode;
        this.status = status;
        this.itemsAmount = itemsAmount != null ? itemsAmount : BigDecimal.ZERO;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.del = del != null ? del : false;
    }

    public void setItemsAmount(BigDecimal itemsAmount) {
        this.itemsAmount = itemsAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
