package co.kr.order.model.repository;

import co.kr.order.model.entity.test.OrderH2Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderH2Entity, Long> {
    
}
