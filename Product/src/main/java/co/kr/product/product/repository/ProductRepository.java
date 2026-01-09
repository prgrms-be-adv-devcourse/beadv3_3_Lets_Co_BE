package co.kr.product.product.repository;

import co.kr.product.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByDelFalse(Pageable pageable);

    Optional<Product> findByProductsIdxAndDelFalse(Long productsIdx);
}




