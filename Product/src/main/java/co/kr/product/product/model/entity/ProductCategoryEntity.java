package co.kr.product.product.model.entity;


import co.kr.product.product.model.vo.CategoryType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Products_Category")
public class ProductCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Category_IDX")
    private Long categoryIdx;

    @Column(name = "Category_Code")
    private String categoryCode;

    @Column(name = "Category_Name")
    private String categoryName;

    @Column(name = "Path")
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type")
    private CategoryType type;

    @Column(name = "Del")
    private Boolean del = false;

    @Column(name = "Parent_IDX")
    private Long parentIdx; // 상위 카테고리/IP

    @Builder
    public ProductCategoryEntity(String categoryCode, String categoryName, String path, CategoryType type, Long parentIdx) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.path = path;
        this.type = type;
        this.parentIdx = parentIdx;
    }

    public void updatePath(String path){

        this.path = path ;
    }

    public void updateName(String name){
        this.categoryName = name;
    }

    public void updateParentIdx(Long parentIdx){
        this.parentIdx = parentIdx;
    }

    public void removeParentIdx(){
        this.parentIdx = null;
    }

    public void delete(){
        this.del = true;
    }

    // 상위 카테고리가 있는지??
    public boolean hasParent() {
        return this.parentIdx != null && this.parentIdx != 0L;
    }


}
