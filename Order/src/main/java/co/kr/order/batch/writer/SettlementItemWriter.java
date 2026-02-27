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
import java.util.List;

/**
 * 정산 처리 Writer
 * - Processor에서 검증 통과된 판매자만 수신
 * - ORDERS_CONFIRMED → SETTLE_PAYOUT으로 상태 업데이트
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

        List<Long> sellerIdxList = chunk.getItems().stream()
                .map(SellerSettlementSummary::getSellerIdx)
                .toList();

        try {
            int updatedCount = settlementRepository.bulkUpdateTypeBySellerIdxListAndPeriod(
                    sellerIdxList,
                    SettlementType.SETTLE_PAYOUT,
                    startDate,
                    endDate
            );

            log.info("Chunk 처리 완료 - 판매자 {}명, 업데이트 건수: {}", sellerIdxList.size(), updatedCount);
        } catch (Exception e) {
            log.error("Chunk 처리 실패 - 판매자 {}명, sellerIdx 범위: [{} ~ {}], 에러: {}",
                    sellerIdxList.size(),
                    sellerIdxList.getFirst(),
                    sellerIdxList.getLast(),
                    e.getMessage());
            throw e;
        }
    }
}
