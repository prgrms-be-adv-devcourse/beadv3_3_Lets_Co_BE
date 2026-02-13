package co.kr.user.dao;

import co.kr.user.model.entity.Payment;
import co.kr.user.model.vo.PaymentStatus;
import co.kr.user.model.vo.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByUsersIdxAndStatusInAndTypeInAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long usersIdx,
            List<PaymentStatus> statusList,
            List<PaymentType> typeList,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}