package co.kr.order.repository;

import co.kr.order.model.entity.OrderEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByOrderCode(String orderCode);
    Optional<OrderEntity> findByUserIdxAndOrderCode(Long userIdx, String orderCode);

    Page<OrderEntity> findAllByUserIdx(Long userIdx, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrderEntity o WHERE o.orderCode = :orderCode")
    Optional<OrderEntity> findByOrderCodeWithLock(@Param("orderCode") String orderCode);
}
