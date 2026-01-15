package co.kr.order.repository;

import co.kr.order.model.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartJpaRepository extends JpaRepository<CartEntity, Long> {

    List<CartEntity> findAllByUserIdx(Long userIdx);

    // userIdx, productIdx, optionIdx에 전부 해당하는 상품 찾기 (N+1 처리 아직 안함)
    Optional<CartEntity> findByUserIdxAndProductIdxAndOptionIdx(Long userIdx, Long productIdx, Long optionIdx);

    void deleteByUserIdx(Long userIdx);
}