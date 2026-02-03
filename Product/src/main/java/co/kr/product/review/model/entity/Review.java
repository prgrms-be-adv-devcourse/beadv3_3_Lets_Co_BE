package co.kr.product.review.model.entity;

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
@Table(name = "Review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Review_IDX")
    private Long reviewIdx;

    @Column(name = "Products_IDX", nullable = false)
    private Long productsIdx;

    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    // 주문아이템당 리뷰 1개 (DB에서도 UNIQUE)
    @Column(name = "Orders_Item_IDX", nullable = false, unique = true)
    private Long ordersItemIdx;

    @Column(name = "Evaluation", nullable = false)
    private Integer evaluation; // 1~5 (DB CHECK)

    @Column(name = "Content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "Created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private Boolean del;

    public Review(Long productsIdx, Long usersIdx, Long ordersItemIdx, Integer evaluation, String content) {
        this.productsIdx = productsIdx;
        this.usersIdx = usersIdx;
        this.ordersItemIdx = ordersItemIdx;
        this.evaluation = evaluation;
        this.content = content;
        this.del = false;
    }

    public void update(Integer evaluation, String content) {
        this.evaluation = evaluation;
        this.content = content;
    }

    public void softDelete() {
        this.del = true;
    }
}




