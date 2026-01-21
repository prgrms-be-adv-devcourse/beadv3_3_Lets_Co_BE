package co.kr.order.batch.config;

import co.kr.order.batch.dto.SellerSettlementSummary;
import co.kr.order.batch.listener.SettlementJobListener;
import co.kr.order.batch.processor.SettlementItemProcessor;
import co.kr.order.batch.util.SettlementTimeUtil;
import co.kr.order.batch.writer.SettlementItemWriter;
import co.kr.order.model.vo.SettlementType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Batch 월간 정산 Job 설정
 * - 매월 15일 새벽에 전월 데이터 정산 처리
 * - 판매자별 집계 후 수수료 계산 및 상태 업데이트
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementBatchConfig {

    private final SettlementItemProcessor settlementItemProcessor;
    private final SettlementJobListener jobListener;

    /**
     * 월간 정산 Job 정의
     */
    @Bean
    public Job monthlySettlementJob(
            JobRepository jobRepository,
            Step settlementProcessStep
    ) {
        return new JobBuilder("monthlySettlementJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(settlementProcessStep)
                .build();
    }

    /**
     * 정산 처리 Step 정의
     * - Chunk 크기: 50 (판매자 50명씩 처리)
     * - Skip 정책: 최대 100건 스킵 허용 (개별 판매자 실패가 전체 영향 없도록)
     */
    @Bean
    public Step settlementProcessStep(

            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<SellerSettlementSummary> settlementReader,
            SettlementItemWriter settlementItemWriter
    ) {
        return new StepBuilder("settlementProcessStep", jobRepository)
                .<SellerSettlementSummary, SellerSettlementSummary>chunk(50, transactionManager)
                .reader(settlementReader)
                .processor(settlementItemProcessor)
                .writer(settlementItemWriter)
                .faultTolerant()
                .skipLimit(100)
                .skip(Exception.class)
                .build();
    }

    /**
     * 판매자별 집계 데이터 Reader
     * - @StepScope: Job 실행 시점에 빈 생성 (날짜 파라미터 동적 바인딩)
     * - 전월 ORDERS_CONFIRMED 데이터를 판매자별로 집계하여 조회
     */
    @Bean
    @StepScope
    public ItemReader<SellerSettlementSummary> settlementReader(
            DataSource dataSource,
            @Value("#{jobParameters['targetMonth']}") String targetMonth
    ) throws Exception {
        // 대상 월 파싱 (예: "2026-01" -> 전월 데이터 조회)
        YearMonth yearMonth = SettlementTimeUtil.resolveTargetMonth(targetMonth);
        LocalDateTime startDate = SettlementTimeUtil.startOfMonth(yearMonth);
        LocalDateTime endDate = SettlementTimeUtil.endOfMonth(yearMonth);

        log.info("정산 대상 기간: {} ~ {}", startDate, endDate);

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
                """);
        queryProviderFactory.setGroupClause("GROUP BY Seller_IDX");
        queryProviderFactory.setSortKey("Seller_IDX");

        reader.setQueryProvider(queryProviderFactory.getObject());

        Map<String, Object> params = new HashMap<>();
        params.put("type", SettlementType.Orders_CONFIRMED.name());
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        reader.setParameterValues(params);

        return reader;
    }
}
