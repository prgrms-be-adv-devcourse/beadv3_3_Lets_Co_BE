package co.kr.customerservice.common.model.entity;

import co.kr.customerservice.common.model.vo.CustomerServiceCategory;
import co.kr.customerservice.common.model.vo.CustomerServiceStatus;
import co.kr.customerservice.common.model.vo.CustomerServiceType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Table(name = "Customer_Service")
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
public class CustomerServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Customer_Service_IDX")
    private Long idx;

    @Column(name = "Customer_Service_Code", nullable = false, length = 64, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type", length = 20)
    private CustomerServiceType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "Category", length = 30)
    private CustomerServiceCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20)
    @ColumnDefault("'DRAFT'") // DB Default
    private CustomerServiceStatus status;

    @Column(name = "Priority", length = 10)
    private String priority;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "Is_Private", nullable = false)
    @ColumnDefault("0")
    private Boolean isPrivate;

    @Column(name = "View_Count", nullable = false)
    @ColumnDefault("0")
    private Long viewCount;

    @Column(name = "Published_at")
    private LocalDateTime publishedAt;

    @Column(name = "Is_Pinned", nullable = false)
    @ColumnDefault("0")
    private Boolean isPinned;

    @Column(name = "Del")
    @ColumnDefault("0")
    private Boolean del;

    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    @Column(name = "User_Name", nullable = false)
    private String userName;

    @Column(name = "Products_IDX")
    private Long productsIdx;


    @Column(name = "Created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_at")
    private LocalDateTime updatedAt;


    @Builder
    public CustomerServiceEntity(String code,
                                 CustomerServiceType type,
                                 CustomerServiceCategory category,
                                 CustomerServiceStatus status,
                                 String priority,
                                 String title,
                                 Boolean isPrivate,
                                 LocalDateTime publishedAt,
                                 Boolean isPinned,
                                 Long usersIdx,
                                 String username,
                                 Long productsIdx) {

        this.code = code;
        this.type = type;
        this.category = category;
        this.status = status;
        this.priority = priority;
        this.title = title;
        this.isPrivate = isPrivate;
        this.publishedAt = publishedAt;
        this.isPinned = isPinned;

        this.usersIdx = usersIdx;
        this.userName = username;
        this.productsIdx = productsIdx;

        this.viewCount = 0L;
        this.del = false;
    }

    public void update(CustomerServiceCategory category,
                       CustomerServiceStatus status,
                       String title,
                       boolean isPrivate,
                       boolean isPinned){

        this.category = category;
        this.status = status;
        this.title = title;
        this.isPrivate = isPrivate;
        this.isPinned = isPinned;
    }

    public void updateStatus(CustomerServiceStatus status){
        this.status = status;
    }

    public void delete(){
        this.del = true;
    }
}
