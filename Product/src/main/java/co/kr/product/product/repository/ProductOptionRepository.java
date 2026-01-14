package co.kr.product.product.repository;

import co.kr.product.product.entity.ProductEntity;
import co.kr.product.product.entity.ProductOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductOptionRepository extends JpaRepository<ProductOptionEntity, Long> {

    List<ProductOptionEntity> findByProductAndDelFalseOrderBySortOrdersAsc(ProductEntity product);

    void deleteByProduct(ProductEntity product);

    List<ProductOptionEntity> findByProductAndDelFalse(ProductEntity product);

}


