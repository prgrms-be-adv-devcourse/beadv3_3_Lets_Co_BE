package co.kr.order.controller;

import co.kr.order.batch.util.SettlementTimeUtil;
import co.kr.order.model.dto.SettlementInfo;
import co.kr.order.model.dto.response.BaseResponse;
import co.kr.order.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/settlement")
public class SettlementController {

    private final SettlementService settlementService;
    private final JobLauncher jobLauncher;
    private final Job monthlySettlementJob;

    @GetMapping("/{sellerIdx}")
    public ResponseEntity<BaseResponse<List<SettlementInfo>>> getSettlementList(
            @PathVariable("sellerIdx") Long sellerIdx) {
        List<SettlementInfo> info = settlementService.getSettlementList(sellerIdx);
        BaseResponse<List<SettlementInfo>> body = new BaseResponse<>("ok", info);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{sellerIdx}/{paymentIdx}")
    public ResponseEntity<BaseResponse<SettlementInfo>> getSettlement(
            @PathVariable("sellerIdx") Long sellerIdx,
            @PathVariable("paymentIdx") Long paymentIdx
    ) {
        SettlementInfo info = settlementService.getSettlement(sellerIdx, paymentIdx);
        BaseResponse<SettlementInfo> body = new BaseResponse<>("ok", info);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/manual")
    public ResponseEntity<BaseResponse<String>> runManualSettlement() {
        YearMonth targetMonth = SettlementTimeUtil.previousMonth();
        String targetMonthStr = targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        try {
            JobExecution execution = jobLauncher.run(monthlySettlementJob,
                    new JobParametersBuilder()
                            .addString("targetMonth", targetMonthStr)
                            .addLong("timestamp", System.currentTimeMillis())
                            .toJobParameters());

            if (execution.getStatus() == BatchStatus.COMPLETED) {
                return ResponseEntity.ok(new BaseResponse<>("ok", "수동 정산 완료 (대상: " + targetMonthStr + " 월" + ")"));
            }
            return ResponseEntity.internalServerError()
                    .body(new BaseResponse<>("fail", "수동 정산 실패: " + execution.getStatus()));

        } catch (Exception e) {
            log.error("수동 정산 실행 중 예외 발생", e);
            return ResponseEntity.internalServerError()
                    .body(new BaseResponse<>("fail", "수동 정산 실행 실패: " + e.getMessage()));
        }
    }
}
