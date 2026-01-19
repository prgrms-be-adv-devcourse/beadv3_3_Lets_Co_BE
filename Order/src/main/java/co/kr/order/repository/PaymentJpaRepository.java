package co.kr.order.repository;

import co.kr.order.model.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByOrdersIdx(Long ordersIdx);

    List<PaymentEntity> findAllByOrdersIdx(Long id);
}