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
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<SettlementHistoryEntity, Long> {

    /** 판매자의 전체 정산 내역 조회 */
    List<SettlementHistoryEntity> findAllBySellerIdx(Long sellerIdx);

    /** 판매자의 특정 결제 건 정산 조회 */
    Optional<SettlementHistoryEntity> findBySellerIdxAndPaymentIdx(Long sellerIdx, Long paymentIdx);

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

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE SettlementHistoryEntity s
            SET s.type = :newType
            WHERE s.sellerIdx IN :sellerIdxList
              AND s.type = co.kr.order.model.vo.SettlementType.ORDERS_CONFIRMED
              AND s.del = false
              AND s.createdAt >= :startDate
              AND s.createdAt <= :endDate
            """)
    int bulkUpdateTypeBySellerIdxListAndPeriod(
            @Param("sellerIdxList") List<Long> sellerIdxList,
            @Param("newType") SettlementType newType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /** 결제 건의 정산 내역 조회 (환불 시 사용) */
    List<SettlementHistoryEntity> findAllByPaymentIdx(Long paymentIdx);
}
