package co.kr.product.product.model.entity;


import co.kr.product.product.model.vo.CategoryType;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "Products_Category")
public class ProductCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Category_IDX")
    private Long categoryId;

    @Column(name = "Category_Code")
    private String categoryCode;

    @Column(name = "Path")
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type")
    private CategoryType type;

    @Column(name = "Del")
    private Boolean del = false;

    @Column(name = "Parent_IDX")
    private Long parentIdx; // 상위 카테고리/IP


}
