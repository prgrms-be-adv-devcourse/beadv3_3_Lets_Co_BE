package co.kr.order.repository;

import co.kr.order.model.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartJpaRepository extends JpaRepository<CartEntity, Long> {

    List<CartEntity> findAllByUserIdx(Long userIdx);

    // 변수 이름이 길어서 JPQL로
    @Query("SELECT c FROM CartEntity c " +
            "WHERE c.userIdx = :userIdx " +
            "AND c.productIdx = :productIdx " +
            "AND c.optionIdx = :optionIdx")
    Optional<CartEntity> findCartEntity(@Param("userIdx") Long userIdx,
                                        @Param("productIdx") Long productIdx,
                                        @Param("optionIdx") Long optionIdx);

    // N+1 문제로 JPQL 사용
    @Modifying(clearAutomatically = true)  // 영속성 컨텍스트를 비움
    @Query("DELETE FROM CartEntity c WHERE c.userIdx = :userIdx")
    void deleteByUserIdx(@Param("userIdx") Long userIdx);
}