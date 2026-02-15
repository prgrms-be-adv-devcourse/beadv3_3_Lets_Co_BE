package co.kr.order.model.entity;

import co.kr.order.model.dto.UserInfo;
import co.kr.order.model.vo.OrderStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @BatchSize(size = 100)  // N+1 방지
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    @Column(name = "Users_IDX", nullable = false)
    private Long userIdx;

    @Column(name = "Card_IDX")
    private Long cardIdx;

    @Column(name = "Orders_Code")
    private String orderCode;

    @Column(name = "Recipient")
    private String recipient;

    @Column(name = "Address")
    private String address;

    @Column(name = "Address_Detail")
    private String addressDetail;

    @Column(name = "Phone_Number")
    private String phone;

    @Enumerated(EnumType.STRING)
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

    @Column(name = "Created_at")
    LocalDateTime createdAt;

    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private Boolean del;

    @Builder
    public OrderEntity(Long userIdx, String orderCode, BigDecimal itemsAmount, BigDecimal totalAmount) {
        this.userIdx = userIdx;
        this.orderCode = orderCode;
        this.recipient = null;
        this.address = null;
        this.addressDetail = null;
        this.phone = null;
        this.status = OrderStatus.CREATED;
        this.itemsAmount = itemsAmount != null ? itemsAmount : BigDecimal.ZERO;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public void setUserData(UserInfo userInfo) {
        this.recipient = userInfo.addressInfo().recipient();
        this.address = userInfo.addressInfo().address();
        this.addressDetail = userInfo.addressInfo().addressDetail();
        this.phone =userInfo.addressInfo().phone();
    }

    public void setCardIdx(Long cardIdx) {
        this.cardIdx = cardIdx;
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

    public void setDel(Boolean del) {
        this.del = del;
    }
}
