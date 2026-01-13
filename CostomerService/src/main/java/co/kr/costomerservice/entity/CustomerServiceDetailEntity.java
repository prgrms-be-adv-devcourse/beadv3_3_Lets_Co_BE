package co.kr.costomerservice.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Table(name = "Customer_Service_Detail")
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
public class CustomerServiceDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Customer_Service_Detail_IDX")
    private Long detailIdx;

    @Column(name = "Customer_Service_Detail_Code", nullable = false, length = 64, unique = true)
    private String detailCode;

    @Column(name = "Parent_IDX")
    private Long parentIdx; // 대댓글/답글의 부모 ID

    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Customer_Service_IDX", nullable = false)
    private CustomerServiceEntity customerService;

    @Lob // MEDIUMTEXT 대응
    @Column(name = "Content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "Del", nullable = false)
    private Boolean del;

    @Column(name = "Created_at",  updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_at" )
    private LocalDateTime updatedAt;


    @Builder
    public CustomerServiceDetailEntity(String detailCode, Long parentIdx, Long usersIdx, CustomerServiceEntity customerService, String content) {
        this.detailCode = detailCode;
        this.parentIdx = parentIdx;
        this.usersIdx = usersIdx;
        this.customerService = customerService;
        this.content = content;
        this.del = false;
    }

    public void update(String content){
        this.content = content;
    }

    public void delete(){
        this.del = true;
    }
}
