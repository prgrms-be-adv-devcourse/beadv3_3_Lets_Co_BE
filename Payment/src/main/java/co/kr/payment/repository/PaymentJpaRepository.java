package co.kr.payment.repository;

import co.kr.payment.model.entity.PaymentEntity;
import co.kr.payment.model.vo.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByOrdersIdx(Long ordersIdx);

    List<PaymentEntity> findAllByOrdersIdx(Long id);

    PaymentEntity findByUsersIdx(Long userIdx);

    Optional<PaymentEntity> findByOrdersIdxAndStatus(Long ordersIdx, PaymentStatus status);
}
