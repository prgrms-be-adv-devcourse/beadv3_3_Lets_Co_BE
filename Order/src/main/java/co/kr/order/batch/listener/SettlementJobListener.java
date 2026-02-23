package co.kr.order.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 월간 정산 배치 Job 리스너
 * - Job 시작/종료 시 로깅
 * - 실패 시 예외 정보 기록
 */
@Slf4j
@Component
public class SettlementJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("=====");
        log.info("월간 정산 배치 Job 시작");
        log.info("Job 이름: {}", jobExecution.getJobInstance().getJobName());
        log.info("Job 파라미터: {}", jobExecution.getJobParameters());
        log.info("시작 시간: {}", jobExecution.getStartTime());
        log.info("=====");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        BatchStatus status = jobExecution.getStatus();
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();

        long durationSeconds = (startTime != null && endTime != null)
                ? Duration.between(startTime, endTime).getSeconds()
                : 0;

        log.info("========================================");
        log.info("월간 정산 배치 Job 완료");
        log.info("Job 상태: {}", status);
        log.info("종료 시간: {}", endTime);
        log.info("소요 시간: {}초", durationSeconds);

        if (status == BatchStatus.FAILED) {
            log.error("Job 실패 - 예외 목록:");
            jobExecution.getAllFailureExceptions()
                    .forEach(ex -> log.error("  - {}", ex.getMessage()));
        }

        log.info("========================================");
    }
}
