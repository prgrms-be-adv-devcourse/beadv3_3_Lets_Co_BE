package co.kr.product.product.repository;

import co.kr.product.product.entity.ProductEntity;
import co.kr.product.product.entity.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {

    List<ProductImageEntity> findByProductAndDelFalseOrderByIsThumbnailDescSortOrdersAsc(
            ProductEntity product);

    ProductImageEntity findAllByProduct(ProductEntity product);

    List<ProductImageEntity> findByProductAndDelFalse(ProductEntity product);
}


