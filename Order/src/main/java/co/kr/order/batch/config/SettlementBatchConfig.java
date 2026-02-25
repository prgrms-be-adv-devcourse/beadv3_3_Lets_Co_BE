package co.kr.order.batch.config;

import co.kr.order.batch.dto.SellerSettlementSummary;
import co.kr.order.batch.listener.SettlementJobListener;
import co.kr.order.batch.listener.SettlementStepListener;
import co.kr.order.batch.partitioner.SellerIdxRangePartitioner;
import co.kr.order.batch.processor.SettlementItemProcessor;
import co.kr.order.batch.util.SettlementTimeUtil;
import co.kr.order.exception.CustomException;
import co.kr.order.batch.writer.SettlementItemWriter;
import co.kr.order.model.vo.SettlementType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Batch 월간 정산 Job 설정 (Partitioning 구조)
 * - Seller_IDX 범위 기반으로 데이터를 분할하여 병렬 처리
 * - Manager Step이 Partitioner로 범위를 분할하고, Worker Step이 각 범위를 독립 처리
 * - Reader/Processor/Writer 모두 병렬로 실행되어 I/O 병목 최소화
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementBatchConfig {

    private final SettlementItemProcessor settlementItemProcessor;
    private final SettlementJobListener jobListener;
    private final SettlementStepListener stepListener;

    /**
     * 월간 정산 Job 정의
     * - Manager Step을 시작점으로 사용
     */
    @Bean
    public Job monthlySettlementJob(
            JobRepository jobRepository,
            Step settlementManagerStep
    ) {
        return new JobBuilder("monthlySettlementJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(settlementManagerStep)
                .build();
    }

    /**
     * Partitioning용 TaskExecutor
     * - corePoolSize: 4 (동시 실행 Worker Step 수)
     * - maxPoolSize: 8 (최대 Worker Step 수)
     * - 각 Worker가 독립된 Reader로 DB를 병렬 조회
     */
    @Bean
    public TaskExecutor settlementTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("settlement-");
        executor.initialize();
        return executor;
    }

    /**
     * Seller_IDX 범위 기반 Partitioner
     * - 대상 기간의 Seller_IDX MIN/MAX를 조회하여 gridSize만큼 분할
     */
    @Bean
    @StepScope
    public Partitioner sellerIdxRangePartitioner(
            DataSource dataSource,
            @Value("#{jobParameters['targetMonth']}") String targetMonth
    ) {
        return new SellerIdxRangePartitioner(dataSource, targetMonth);
    }

    /**
     * Manager Step - Partitioner로 Worker Step을 분산 실행
     * - gridSize: 4 (Seller_IDX 범위를 4등분)
     * - TaskExecutor로 Worker Step 병렬 실행
     */
    @Bean
    public Step settlementManagerStep(
            JobRepository jobRepository,
            Step settlementWorkerStep,
            Partitioner sellerIdxRangePartitioner
    ) {
        return new StepBuilder("settlementManagerStep", jobRepository)
                .partitioner("settlementWorkerStep", sellerIdxRangePartitioner)
                .step(settlementWorkerStep)
                .gridSize(4)
                .taskExecutor(settlementTaskExecutor())
                .build();
    }

    /**
     * Worker Step - 파티션 범위 내 데이터를 Chunk 단위로 처리
     * - Chunk 크기: 50 (판매자 50명씩 처리)
     * - Skip 정책: 최대 100건 스킵 허용 (개별 판매자 실패가 전체 영향 없도록)
     */
    @Bean
    public Step settlementWorkerStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<SellerSettlementSummary> settlementReader,
            SettlementItemWriter settlementItemWriter
    ) {
        return new StepBuilder("settlementWorkerStep", jobRepository)
                .<SellerSettlementSummary, SellerSettlementSummary>chunk(50, transactionManager)
                .reader(settlementReader)
                .processor(settlementItemProcessor)
                .writer(settlementItemWriter)
                .listener(stepListener)
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .noRetry(CustomException.class)
                .skipLimit(100)
                .skip(CustomException.class)
                .noSkip(Exception.class)
                .build();
    }

    /**
     * 판매자별 집계 데이터 Reader (파티션 범위 적용)
     * - @StepScope: 각 파티션별 독립 인스턴스 생성
     * - stepExecutionContext에서 minSellerIdx/maxSellerIdx를 받아 범위 조회
     * - 전월 ORDERS_CONFIRMED 데이터를 판매자별로 집계
     */
    @Bean
    @StepScope
    public ItemReader<SellerSettlementSummary> settlementReader(
            DataSource dataSource,
            @Value("#{jobParameters['targetMonth']}") String targetMonth,
            @Value("#{stepExecutionContext['minSellerIdx']}") Long minSellerIdx,
            @Value("#{stepExecutionContext['maxSellerIdx']}") Long maxSellerIdx
    ) throws Exception {
        YearMonth yearMonth = SettlementTimeUtil.resolveTargetMonth(targetMonth);
        LocalDateTime startDate = SettlementTimeUtil.startOfMonth(yearMonth);
        LocalDateTime endDate = SettlementTimeUtil.endOfMonth(yearMonth);

        log.info("정산 Reader 초기화 - 기간: {} ~ {}, Seller_IDX 범위: [{} ~ {}]",
                startDate, endDate, minSellerIdx, maxSellerIdx);

        JdbcPagingItemReader<SellerSettlementSummary> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setName("settlementReader");
        reader.setPageSize(500);
        reader.setRowMapper((rs, rowNum) -> new SellerSettlementSummary(
                rs.getLong("seller_idx"),
                rs.getBigDecimal("total_amount"),
                rs.getLong("record_count")
        ));

        SqlPagingQueryProviderFactoryBean queryProviderFactory = new SqlPagingQueryProviderFactoryBean();
        queryProviderFactory.setDataSource(dataSource);
        queryProviderFactory.setSelectClause("""
                SELECT Seller_IDX AS seller_idx,
                       SUM(Amount) AS total_amount,
                       COUNT(*) AS record_count
                """);
        queryProviderFactory.setFromClause("FROM Settlement_History");
        queryProviderFactory.setWhereClause("""
                WHERE Type = :type
                  AND Del = false
                  AND Created_at >= :startDate
                  AND Created_at <= :endDate
                  AND Seller_IDX >= :minSellerIdx
                  AND Seller_IDX <= :maxSellerIdx
                """);
        queryProviderFactory.setGroupClause("GROUP BY Seller_IDX");
        queryProviderFactory.setSortKey("Seller_IDX");

        reader.setQueryProvider(queryProviderFactory.getObject());

        Map<String, Object> params = new HashMap<>();
        params.put("type", SettlementType.ORDERS_CONFIRMED.name());
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("minSellerIdx", minSellerIdx);
        params.put("maxSellerIdx", maxSellerIdx);
        reader.setParameterValues(params);

        return reader;
    }
}
