package co.kr.order.batch.writer;

import co.kr.order.batch.dto.SellerSettlementSummary;
import co.kr.order.batch.util.SettlementTimeUtil;
import co.kr.order.model.vo.SettlementType;
import co.kr.order.repository.SettlementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * 정산 처리 Writer
 * - 검증 성공 시: SETTLE_PAYOUT으로 상태 업데이트
 * - 검증 실패 시: 상태 유지 + 에러 로그
 */
@Slf4j
@Component
@StepScope
public class SettlementItemWriter implements ItemWriter<SellerSettlementSummary> {

    private final SettlementRepository settlementRepository;
    private final String targetMonth;

    public SettlementItemWriter(
            SettlementRepository settlementRepository,
            @Value("#{jobParameters['targetMonth']}") String targetMonth
    ) {
        this.settlementRepository = settlementRepository;
        this.targetMonth = targetMonth;
    }

    @Override
    public void write(@NonNull Chunk<? extends SellerSettlementSummary> chunk) throws Exception {
        // 대상 기간 계산
        YearMonth yearMonth = SettlementTimeUtil.resolveTargetMonth(targetMonth);
        LocalDateTime startDate = SettlementTimeUtil.startOfMonth(yearMonth);
        LocalDateTime endDate = SettlementTimeUtil.endOfMonth(yearMonth);

        int successCount = 0;
        int failCount = 0;

        for (SellerSettlementSummary summary : chunk) {
            Long sellerIdx = summary.getSellerIdx();

            if (summary.isValidated()) {
                // 검증 성공: SETTLE_PAYOUT으로 업데이트
                int updatedCount = settlementRepository.updateTypeBySellerIdxAndPeriod(
                        sellerIdx,
                        SettlementType.SETTLE_PAYOUT,
                        startDate,
                        endDate
                );

                log.info("판매자 {} 정산 완료 - 업데이트 건수: {}, 지급액: {}",
                        sellerIdx, updatedCount, summary.getPayoutAmount());

                successCount++;

            } else {
                // 검증 실패: 상태 유지 (ORDERS_CONFIRMED -> 에러 로그
                log.warn("판매자 {} 정산 보류 - 사유: {}, 총액: {}, 레코드 수: {}",
                        sellerIdx,
                        summary.getValidationMessage(),
                        summary.getTotalAmount(),
                        summary.getRecordCount());

                failCount++;
            }
        }

        log.info("Chunk 처리 완료 - 성공: {}, 실패: {}", successCount, failCount);
    }
}
