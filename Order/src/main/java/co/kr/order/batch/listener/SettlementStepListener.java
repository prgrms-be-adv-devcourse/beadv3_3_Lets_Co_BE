package co.kr.order.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Worker Step 성능 추적 리스너
 * - 파티션별 처리 시작/종료 시간, 소요 시간
 * - Read/Write/Skip 건수
 * - 파티션 범위 (Seller_IDX min ~ max)
 */
@Slf4j
@Component
public class SettlementStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        Long minSellerIdx = stepExecution.getExecutionContext().getLong("minSellerIdx", -1);
        Long maxSellerIdx = stepExecution.getExecutionContext().getLong("maxSellerIdx", -1);

        log.info("[{}] 파티션 시작 - Seller_IDX 범위: [{} ~ {}], 스레드: {}",
                stepExecution.getStepName(),
                minSellerIdx, maxSellerIdx,
                Thread.currentThread().getName());
    }

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        Long minSellerIdx = stepExecution.getExecutionContext().getLong("minSellerIdx", -1);
        Long maxSellerIdx = stepExecution.getExecutionContext().getLong("maxSellerIdx", -1);

        LocalDateTime startTime = stepExecution.getStartTime();
        LocalDateTime endTime = stepExecution.getEndTime();
        long durationMs = (startTime != null && endTime != null)
                ? Duration.between(startTime, endTime).toMillis()
                : 0;

        log.info("[{}] 파티션 완료 - Seller_IDX [{} ~ {}] | 소요: {}ms | Read: {} | Write: {} | Filter: {} | Skip: {} | 상태: {}",
                stepExecution.getStepName(),
                minSellerIdx, maxSellerIdx,
                durationMs,
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getFilterCount(),
                stepExecution.getSkipCount(),
                stepExecution.getStatus());

        return stepExecution.getExitStatus();
    }
}
