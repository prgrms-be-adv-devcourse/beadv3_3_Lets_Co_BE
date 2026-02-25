package co.kr.order.repository;

import co.kr.order.model.entity.OrderEntity;
import co.kr.order.model.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, Long> {

    List<OrderItemEntity> findAllByOrder(OrderEntity order);

    Optional<OrderItemEntity> findByProductCode(String productsCode);
}
