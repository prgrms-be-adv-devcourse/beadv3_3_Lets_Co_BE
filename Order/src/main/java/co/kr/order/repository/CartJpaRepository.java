package co.kr.order.repository;

import co.kr.order.model.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartJpaRepository extends JpaRepository<CartEntity, Long> {

    List<CartEntity> findAllByUserIdx(Long userIdx);
    Optional<CartEntity> findByUserIdxAndProductIdxAndOptionIdx(Long userIdx, Long productIdx, Long optionIdx);
}