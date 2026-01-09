package co.kr.product.product.repository;

import co.kr.product.product.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    List<ProductOption> findByProductsIdxAndDelFalseOrderBySortOrderAsc(Long productsIdx);
}


