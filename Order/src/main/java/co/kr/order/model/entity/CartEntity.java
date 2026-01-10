package co.kr.order.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "Cart")
public class CartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Cart_Item_IDX")
    private Long id;

    @Column(name = "Users_IDX", nullable = false)
    private Long userIdx;

    @Column(name = "Products_IDX", nullable = false)
    private Long productIdx;

    @Column(name = "Option_Group_IDX", nullable = false)
    private Long optionIdx;

    @Column(name = "Price", precision = 19, scale = 2, nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

//    @Column(name = "Sale_Price", precision = 19, scale = 2)
//    private BigDecimal salePrice;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "Del", nullable = false)
    Boolean del;

    public void plusQuantity() {
        this.quantity++;
    }

    public void minusQuantity() {
        this.quantity--;
    }

    public void addPrice(BigDecimal price) {
        if (price == null) return;
        this.price = this.price.add(price);
    }

    public void subtractPrice(BigDecimal price) {
        if (price == null) return;
        this.price = this.price.subtract(price);
    }

    public BigDecimal getTotalPrice(BigDecimal unitPrice) {
        return unitPrice.multiply(new BigDecimal(this.quantity));
    }
}
