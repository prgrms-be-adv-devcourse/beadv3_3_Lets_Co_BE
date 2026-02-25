package co.kr.order.batch.cache;

import co.kr.order.batch.util.SettlementTimeUtil;
import co.kr.order.client.UserClient;
import co.kr.order.model.dto.response.SellerBulkResponse;
import co.kr.order.model.dto.SellerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 파티션 단위 판매자 정보 캐시
 * - Worker Step 생성 시 해당 파티션 범위의 판매자 ID를 조회
 * - User 서비스에 Bulk 요청 1회로 전체 판매자 계좌 정보를 가져옴
 * - Processor에서 Map 조회만으로 판매자 검증 수행 (Feign 호출 0회)
 */
@Slf4j
@Component
@StepScope
public class SellerInfoCache {

    private final Map<Long, SellerInfo> cache;

    public SellerInfoCache(
            UserClient userClient,
            JdbcTemplate jdbcTemplate,
            @Value("#{stepExecutionContext['minSellerIdx']}") Long minSellerIdx,
            @Value("#{stepExecutionContext['maxSellerIdx']}") Long maxSellerIdx,
            @Value("#{jobParameters['targetMonth']}") String targetMonth
    ) {
        YearMonth yearMonth = SettlementTimeUtil.resolveTargetMonth(targetMonth);
        LocalDateTime startDate = SettlementTimeUtil.startOfMonth(yearMonth);
        LocalDateTime endDate = SettlementTimeUtil.endOfMonth(yearMonth);

        // 파티션 범위 내 대상 판매자 ID 조회
        List<Long> sellerIds = jdbcTemplate.queryForList(
                """
                SELECT DISTINCT Seller_IDX
                FROM Settlement_History
                WHERE Type = 'ORDERS_CONFIRMED'
                  AND Del = false
                  AND Created_at >= ?
                  AND Created_at <= ?
                  AND Seller_IDX >= ?
                  AND Seller_IDX <= ?
                """,
                Long.class,
                startDate, endDate, minSellerIdx, maxSellerIdx
        );

        if (sellerIds.isEmpty()) {
            log.info("파티션 [{} ~ {}] 대상 판매자 없음", minSellerIdx, maxSellerIdx);
            this.cache = Collections.emptyMap();
            return;
        }

        log.info("파티션 [{} ~ {}] 판매자 {}건 Bulk 조회 시작", minSellerIdx, maxSellerIdx, sellerIds.size());

        Map<Long, SellerInfo> fetched;
        try {
            SellerBulkResponse response = userClient.getSellerDataBulk(sellerIds);

            if (response != null && "SUCCESS".equals(response.resultCode()) && response.data() != null) {
                fetched = response.data().stream()
                        .collect(Collectors.toMap(SellerInfo::sellerIdx, info -> info));
                log.info("판매자 정보 캐시 완료 - {}건", fetched.size());
            } else {
                log.error("판매자 Bulk 조회 실패 - resultCode: {}",
                        response != null ? response.resultCode() : "null");
                fetched = Collections.emptyMap();
            }
        } catch (Exception e) {
            log.error("판매자 Bulk 조회 중 예외 발생: {}", e.getMessage());
            fetched = Collections.emptyMap();
        }
        this.cache = fetched;
    }

    public SellerInfo get(Long sellerIdx) {
        return cache.get(sellerIdx);
    }
}
