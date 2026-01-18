package co.kr.order.repository;

import co.kr.order.model.entity.SettlementHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 정산 내역 Repository
 */
@Repository
public interface SettlementRepository extends JpaRepository<SettlementHistoryEntity, Long> {

    // 추후 조회 API 개발 시 메서드 추가 예정
    // - findBySellerIdxAndCreatedAtBetween (기간별 조회)
    // - sumAmountBySellerAndType (요약 조회)
}
