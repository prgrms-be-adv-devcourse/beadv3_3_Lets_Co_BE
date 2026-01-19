package co.kr.user.DAO;

import co.kr.user.model.entity.Payment;
import co.kr.user.model.vo.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 결제 및 충전 내역(Payment) 엔티티의 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 * 사용자의 포인트 충전 이력, 상품 구매 결제 내역 등을 조회하는 기능을 제공합니다.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    /**
     * 특정 사용자의 특정 유형(Type)에 해당하는 모든 결제 내역을 최신순으로 조회하는 메서드입니다.
     * 예: 사용자의 모든 '충전(DEPOSIT)' 내역을 조회하여 잔액 변동 이력을 보여줄 때 사용됩니다.
     *
     * @param usersIdx 사용자 고유 식별자
     * @param type 결제 유형 (예: CARD, DEPOSIT 등)
     * @return 조건에 맞는 결제 내역 리스트 (최신순 정렬)
     */
    List<Payment> findAllByUsersIdxAndTypeOrderByCreatedAtDesc(Long usersIdx, PaymentType type);

    /**
     * 특정 기간(Start ~ End) 동안 발생한 특정 유형의 결제 내역을 최신순으로 조회하는 메서드입니다.
     * 사용자가 날짜 필터를 적용하여 이력을 검색할 때(예: 최근 1개월 충전 내역 등) 사용됩니다.
     *
     * @param usersIdx 사용자 고유 식별자
     * @param type 결제 유형
     * @param startDate 조회 시작 일시
     * @param endDate 조회 종료 일시
     * @return 해당 기간 내의 결제 내역 리스트 (최신순 정렬)
     */
    List<Payment> findAllByUsersIdxAndTypeAndCreatedAtBetweenOrderByCreatedAtDesc(Long usersIdx, PaymentType type,
                                                                                  LocalDateTime startDate, LocalDateTime endDate);
}