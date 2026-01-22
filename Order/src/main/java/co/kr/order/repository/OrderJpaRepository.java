package co.kr.order.repository;

import co.kr.order.model.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderCode(String orderCode);
    List<OrderEntity> findAllByUserIdx(Long userIdx);
    Optional<OrderEntity> findByUserIdxAndOrderCode(Long userIdx, String orderCode);
}
