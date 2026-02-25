package co.kr.order.batch.partitioner;

import co.kr.order.batch.util.SettlementTimeUtil;
import co.kr.order.model.vo.SettlementType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

/**
 * Seller_IDX 범위 기반 Partitioner
 * - Settlement_History에서 대상 기간의 MIN/MAX Seller_IDX를 조회
 * - gridSize만큼 범위를 균등 분할하여 각 Worker Step에 할당
 * - 데이터가 없으면 빈 Map 반환 (Worker Step 실행 안 됨)
 */
@Slf4j
public class SellerIdxRangePartitioner implements Partitioner {

    private final JdbcTemplate jdbcTemplate;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public SellerIdxRangePartitioner(DataSource dataSource, String targetMonth) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        YearMonth yearMonth = SettlementTimeUtil.resolveTargetMonth(targetMonth);
        this.startDate = SettlementTimeUtil.startOfMonth(yearMonth);
        this.endDate = SettlementTimeUtil.endOfMonth(yearMonth);
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result = new HashMap<>();

        Long minId = jdbcTemplate.queryForObject(
                "SELECT MIN(Seller_IDX) FROM Settlement_History " +
                        "WHERE Type = ? AND Del = false AND Created_at >= ? AND Created_at <= ?",
                Long.class,
                SettlementType.ORDERS_CONFIRMED.name(), startDate, endDate
        );

        Long maxId = jdbcTemplate.queryForObject(
                "SELECT MAX(Seller_IDX) FROM Settlement_History " +
                        "WHERE Type = ? AND Del = false AND Created_at >= ? AND Created_at <= ?",
                Long.class,
                SettlementType.ORDERS_CONFIRMED.name(), startDate, endDate
        );

        if (minId == null || maxId == null) {
            log.info("정산 대상 데이터 없음 - 파티션 생성 건너뜀");
            return result;
        }

        long range = maxId - minId;
        long partitionSize = (range / gridSize) + 1;

        log.info("파티션 분할 - Seller_IDX 범위: [{} ~ {}], gridSize: {}, 파티션 크기: {}",
                minId, maxId, gridSize, partitionSize);

        for (int i = 0; i < gridSize; i++) {
            long start = minId + (i * partitionSize);
            long end = Math.min(start + partitionSize - 1, maxId);

            ExecutionContext context = new ExecutionContext();
            context.putLong("minSellerIdx", start);
            context.putLong("maxSellerIdx", end);
            result.put("partition" + i, context);

            log.debug("파티션 {} 생성 - Seller_IDX 범위: [{} ~ {}]", i, start, end);

            if (end >= maxId) break;
        }

        return result;
    }
}
