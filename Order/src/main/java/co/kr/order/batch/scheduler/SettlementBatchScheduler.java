package co.kr.order.batch.scheduler;

import co.kr.order.batch.util.SettlementTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * 월간 정산 배치 스케줄러
 * - 매월 15일 새벽 3시에 전월 정산 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job monthlySettlementJob;

    @Scheduled(cron = "${custom.batch.settlement.cron:0 0 3 15 * ?}")
    public void runMonthlySettlementJob() {
        log.info("===== 월간 정산 배치 스케줄 실행 시작 =====");

        try {
            // 전월 계산 (예: 현재 2026-02-15 -> 대상월 2026-01)
            YearMonth targetMonth = SettlementTimeUtil.previousMonth();
            String targetMonthStr = targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("targetMonth", targetMonthStr)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            log.info("정산 대상 : {} 월", targetMonthStr);

            jobLauncher.run(monthlySettlementJob, jobParameters);

            log.info("===== 월간 정산 배치 스케줄 실행 완료 =====");

        } catch (Exception e) {
            log.error("월간 정산 배치 실행 실패", e);
        }
    }

    }
