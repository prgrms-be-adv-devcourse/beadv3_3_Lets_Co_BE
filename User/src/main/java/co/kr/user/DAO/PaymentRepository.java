package co.kr.user.DAO;

import co.kr.user.model.entity.Payment;
import co.kr.user.model.vo.PaymentStatus;
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
     * 특정 기간(Start ~ End) 동안 발생한 특정 유형의 결제 내역을 최신순으로 조회하는 메서드입니다.
     * 사용자가 날짜 필터를 적용하여 이력을 검색할 때(예: 최근 1개월 충전 내역 등) 사용됩니다.
     *
     * @param usersIdx 사용자 고유 식별자
     * @param startDate 조회 시작 일시
     * @param endDate 조회 종료 일시
     * @return 해당 기간 내의 결제 내역 리스트 (최신순 정렬)
     */
    // 3. [수정됨] 여러 상태(Status List)와 여러 타입(Type List) 및 기간 조회
    // 'Status' -> 'StatusIn', 'Type' -> 'TypeIn'으로 변경하여 List 타입을 처리하도록 수정
    List<Payment> findAllByUsersIdxAndStatusInAndTypeInAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long usersIdx,
            List<PaymentStatus> statusList,
            List<PaymentType> typeList,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}