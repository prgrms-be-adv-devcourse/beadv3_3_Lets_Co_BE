package co.kr.product.product.repository;

import co.kr.product.product.model.entity.ProductCategoryEntity;
import co.kr.product.product.model.vo.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategoryEntity, Long> {

    List<ProductCategoryEntity> findAllByTypeAndDelFalse(CategoryType categoryType);

    Optional<ProductCategoryEntity> findByCategoryCodeAndTypeAndDelFalse(String s, CategoryType categoryType);

    @Modifying(clearAutomatically = true)
    @Query(value =  "UPDATE Products_Category " +
                    "SET Path = CONCAT(:parentPath, SUBSTRING(Path, LENGTH(:oldParentPath)+1 )) " +
                    "WHERE Path LIKE CONCAT(:oldPath,'%') ",
            nativeQuery = true)
    int updateChildPath(@Param("oldPath") String oldPath,
                        @Param("parentPath") String parentPath,
                        @Param("oldParentPath") String  oldParentPath);


    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductCategoryEntity c " +
            "SET c.del = true " +
            "WHERE c.path LIKE CONCAT(:parentPath,'%')")
    int updateChildDel(@Param("parentPath") String parentPath);

    List<ProductCategoryEntity> findAllByCategoryIdxInAndTypeAndDelFalse(List<Long> parentsIdx, CategoryType type);

    List<ProductCategoryEntity> findAllByPathStartingWithAndTypeAndDelFalse(String path, CategoryType type);

    List<ProductCategoryEntity> findAllByParentIdxAndTypeAndDelFalse(Long parentIdx, CategoryType categoryType);
}
