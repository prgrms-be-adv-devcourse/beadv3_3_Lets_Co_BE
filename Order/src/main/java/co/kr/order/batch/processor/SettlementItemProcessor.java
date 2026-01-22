package co.kr.order.batch.processor;

import co.kr.order.batch.dto.SellerSettlementSummary;
import co.kr.order.client.UserClient;
import co.kr.order.model.dto.SellerInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * 정산 처리 Processor
 * - 수수료 계산 (판매자별 합산 금액 × 0.98, 소수점 버림)
 * - 판매자 계좌 정보 검증
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementItemProcessor implements ItemProcessor<SellerSettlementSummary, SellerSettlementSummary> {

    private final UserClient userClient;
    @Value("${custom.settlement.fee-rate:0.02}")
    private BigDecimal settlementFeeRate;

        @Override
    public SellerSettlementSummary process(@NonNull SellerSettlementSummary item) throws Exception {
        Long sellerIdx = item.getSellerIdx();
        BigDecimal totalAmount = item.getTotalAmount();

        log.debug("판매자 {} 정산 처리 시작 - 총액: {}, 레코드 수: {}",
                sellerIdx, totalAmount, item.getRecordCount());

        // 수수료 계산
        BigDecimal feeMultiplier = BigDecimal.ONE.subtract(settlementFeeRate);
        BigDecimal payoutAmount = totalAmount
                .multiply(feeMultiplier)
                .setScale(0, RoundingMode.FLOOR);

        log.debug("판매자 {} 수수료 차감 - 수수료율: {}, 지급액: {}",
                sellerIdx, settlementFeeRate, payoutAmount);


        // Seller Bank 정보 검증
        try {
            SellerInfo sellerInfo = userClient.getSellerData(Set.of(sellerIdx));

            if (sellerInfo == null) {
                log.error("판매자 정보 조회 실패 - sellerIdx: {}", sellerIdx);
                return item.withPayoutResult(payoutAmount, false,
                        "판매자 정보를 찾을 수 없습니다");
            }

            // 계좌 정보 검증: Bank_Brand 존재 여부
            if (sellerInfo.bankBrand() == null || sellerInfo.bankBrand().isBlank()) {
                log.error("판매자 계좌 브랜드 누락 - sellerIdx: {}", sellerIdx);
                return item.withPayoutResult(payoutAmount, false,
                        "은행 브랜드 정보가 없습니다");
            }

            // 계좌 정보 검증: Bank_Token 존재 여부
            if (sellerInfo.bankToken() == null || sellerInfo.bankToken().isBlank()) {
                log.error("판매자 계좌 토큰 누락 - sellerIdx: {}", sellerIdx);
                return item.withPayoutResult(payoutAmount, false,
                        "계좌 토큰 정보가 없습니다");
            }

            log.info("판매자 {} 검증 성공 - 은행: {}, 지급액: {}",
                    sellerIdx, sellerInfo.bankBrand(), payoutAmount);

            return item.withPayoutResult(payoutAmount, true, null);

        } catch (Exception e) {
            log.error("판매자 {} 정보 조회 중 예외 발생: {}", sellerIdx, e.getMessage());
            return item.withPayoutResult(payoutAmount, false,
                    "판매자 정보 조회 실패: " + e.getMessage());
        }
    }
}
