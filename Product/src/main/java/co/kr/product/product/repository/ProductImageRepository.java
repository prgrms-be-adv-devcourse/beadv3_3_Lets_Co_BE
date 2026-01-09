package co.kr.product.product.repository;

import co.kr.product.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductsIdxAndDelFalseOrderByIsThumbnailDescSortOrderAsc(
            Long productsIdx);
}


