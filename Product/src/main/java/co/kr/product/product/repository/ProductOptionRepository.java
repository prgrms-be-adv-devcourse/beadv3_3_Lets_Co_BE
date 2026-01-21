package co.kr.product.product.repository;

import co.kr.product.product.entity.ProductEntity;
import co.kr.product.product.entity.ProductOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductOptionRepository extends JpaRepository<ProductOptionEntity, Long> {

    List<ProductOptionEntity> findByProductAndDelFalseOrderBySortOrdersAsc(ProductEntity product);

    void deleteByProduct(ProductEntity product);

    List<ProductOptionEntity> findByProductAndDelFalse(ProductEntity product);

    Optional<ProductOptionEntity> findByOptionGroupIdxAndDelFalse(Long aLong);

    @Modifying
    @Query("UPDATE ProductOptionEntity p"+
            " SET p.stock = p.stock - :quantity"+
            " WHERE p.optionGroupIdx = :id " +
            "AND p.stock >= :quantity " +
            "AND p.del = false")
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    List<ProductOptionEntity> findByOptionGroupIdxInAndDelFalse(List<Long> optionIds);


    // fetch join
    @Query("SELECT o FROM ProductOptionEntity o JOIN FETCH o.product p " +
            "WHERE o.optionGroupIdx IN :optionIds AND o.del = false AND p.del = false")
    List<ProductOptionEntity> findAllWithOptions(@Param("optionIds") List<Long> optionIds);
}


