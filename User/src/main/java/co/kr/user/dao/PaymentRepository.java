package co.kr.user.dao;

import co.kr.user.model.entity.Payment;
import co.kr.user.model.vo.PaymentStatus;
import co.kr.user.model.vo.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment 엔티티(결제/충전 내역)의 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 * Spring Data JPA를 상속받아 기본적인 CRUD 기능을 제공합니다.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 특정 사용자의 결제/충전 내역을 다양한 조건으로 조회합니다.
     * 상태 목록, 유형 목록, 기간 조건을 모두 만족하는 데이터를 최신순(생성일 내림차순)으로 정렬하여 반환합니다.
     *
     * @param usersIdx 사용자 식별자 (PK)
     * @param statusList 조회할 결제 상태 목록 (예: PAYMENT, CHARGE, REFUND)
     * @param typeList 조회할 결제 수단 유형 목록 (예: CARD, DEPOSIT 등)
     * @param startDate 조회 시작 일시
     * @param endDate 조회 종료 일시
     * @return 조건에 맞는 Payment 엔티티 리스트 (최신순 정렬)
     */
    List<Payment> findAllByUsersIdxAndStatusInAndTypeInAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long usersIdx,
            List<PaymentStatus> statusList,
            List<PaymentType> typeList,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}