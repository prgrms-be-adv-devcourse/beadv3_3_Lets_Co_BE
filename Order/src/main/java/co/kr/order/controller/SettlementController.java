package co.kr.order.controller;

import co.kr.order.batch.util.SettlementTimeUtil;
import co.kr.order.model.dto.SettlementInfo;
import co.kr.order.model.dto.response.BaseResponse;
import co.kr.order.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 정산 관련 API 컨트롤러
 *- 판매자 정산 내역 목록 조회
 *- 특정 결제 건 정산 상세 조회
 *- 정산 배치 수동 실행 (운영/데모 목적)
 * 수동 배치는 동기 방식으로 실행되며,
 * 기본 대상 월은 전월 기준으로 고정한다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/settlement")
public class SettlementController {

    private final SettlementService settlementService;
    private final JobLauncher jobLauncher;
    private final Job monthlySettlementJob;

    /**
     * 특정 판매자의 정산 내역 목록을 조회한다.
     */
    @GetMapping("/{sellerIdx}")
    public ResponseEntity<BaseResponse<List<SettlementInfo>>> getSettlementList(
            @PathVariable("sellerIdx") Long sellerIdx) {

        List<SettlementInfo> info = settlementService.getSettlementList(sellerIdx);
        return ResponseEntity.ok(new BaseResponse<>("ok", info));
    }

    /**
     * 특정 판매자의 결제 건에 대한 정산 상세 정보를 조회한다.
     */
    @GetMapping("/{sellerIdx}/{paymentIdx}")
    public ResponseEntity<BaseResponse<SettlementInfo>> getSettlement(
            @PathVariable("sellerIdx") Long sellerIdx,
            @PathVariable("paymentIdx") Long paymentIdx
    ) {

        SettlementInfo info = settlementService.getSettlement(sellerIdx, paymentIdx);
        return ResponseEntity.ok(new BaseResponse<>("ok", info));
    }

    /**
     * 전월 기준으로 정산 배치를 수동 실행한다.
     * - 데모 목적
     * - 요청 스레드에서 동기 실행
     * - Job 중복 실행 방지를 위해 timestamp 파라미터를 포함한다.
     */
    @PostMapping("/manual")
    public ResponseEntity<BaseResponse<String>> runManualSettlement() {

        YearMonth targetMonth = SettlementTimeUtil.previousMonth();
        String targetMonthStr = targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        try {
            JobExecution execution = jobLauncher.run(
                    monthlySettlementJob,
                    new JobParametersBuilder()
                            .addString("targetMonth", targetMonthStr)
                            .addLong("timestamp", System.currentTimeMillis())
                            .toJobParameters()
            );

            if (execution.getStatus() == BatchStatus.COMPLETED) {
                long successCount = 0;
                long filterCount = 0;

                for (StepExecution step : execution.getStepExecutions()) {
                    if (step.getStepName().startsWith("settlementWorkerStep")) {
                        successCount += step.getWriteCount();
                        filterCount += step.getFilterCount();
                    }
                }

                String message = String.format(
                        "수동 정산 완료 (대상월: %s) - 정산 완료: %d명, 보류: %d명 (총 판매자: %d명)",
                        targetMonthStr, successCount, filterCount, successCount + filterCount
                );
                return ResponseEntity.ok(new BaseResponse<>("ok", message));
            }

            return ResponseEntity.internalServerError()
                    .body(new BaseResponse<>("fail",
                            "수동 정산 실패: " + execution.getStatus()));

        } catch (Exception e) {
            log.error("수동 정산 실행 중 예외 발생", e);
            return ResponseEntity.internalServerError()
                    .body(new BaseResponse<>("fail",
                            "수동 정산 실행 실패: " + e.getMessage()));
        }
    }
}