package co.kr.product.product.repository;

import co.kr.product.product.entity.ProductEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Page<ProductEntity> findByDelFalse(Pageable pageable);

    Optional<ProductEntity> findByProductsIdxAndDelFalse(Long productsIdx);

    Optional<ProductEntity> findByProductsCodeAndDelFalse(String productsCode);

    String productsCode(String productsCode);

    List<ProductEntity> findByProductsIdxInAndDelFalse(List<Long> productIds);
}




