package co.kr.order.repository;

import co.kr.order.model.entity.SettlementHistoryEntity;
import co.kr.order.model.vo.SettlementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<SettlementHistoryEntity, Long> {

    List<SettlementHistoryEntity> findAllBySellerIdx(Long sellerIdx);

    SettlementHistoryEntity findBySellerIdxAndPaymentIdx(Long sellerIdx, Long paymentIdx);

    /**
     * Update settlement type (ORDERS_CONFIRMED -> SETTLE_PAYOUT).
     * JPQL UPDATE를 수행하는데, @Modifying이 없으면 Spring Data가 조회 쿼리로 취급해서 실행이 안 됩니다
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE SettlementHistoryEntity s
            SET s.type = :newType
            WHERE s.sellerIdx = :sellerIdx
              AND s.type = co.kr.order.model.vo.SettlementType.ORDERS_CONFIRMED
              AND s.del = false
              AND s.createdAt >= :startDate
              AND s.createdAt <= :endDate
            """)
    int updateTypeBySellerIdxAndPeriod(
            @Param("sellerIdx") Long sellerIdx,
            @Param("newType") SettlementType newType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

}
