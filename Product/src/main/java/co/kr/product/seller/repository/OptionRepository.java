package co.kr.product.seller.repository;

import co.kr.product.seller.entity.OptionEntity;
import co.kr.product.seller.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionRepository extends JpaRepository<OptionEntity, Long> {
    List<OptionEntity> findByProduct(ProductEntity product);

    void deleteByProduct(ProductEntity product);
}
