package co.kr.user.DAO;

import co.kr.user.model.entity.Payment;
import co.kr.user.model.vo.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository  extends JpaRepository<Payment, Long> {
    List<Payment> findAllByUsersIdxAndTypeOrderByCreatedAtDesc(Long usersIdx, PaymentType type);

    List<Payment> findAllByUsersIdxAndTypeAndCreatedAtBetweenOrderByCreatedAtDesc(Long usersIdx, PaymentType type,
                                                              LocalDateTime startDate, LocalDateTime endDate);
}