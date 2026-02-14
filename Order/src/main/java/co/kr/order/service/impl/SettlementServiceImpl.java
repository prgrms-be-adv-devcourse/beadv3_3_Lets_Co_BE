package co.kr.order.service.impl;

import co.kr.order.exception.CustomException;
import co.kr.order.exception.ErrorCode;
import co.kr.order.model.dto.SettlementInfo;
import co.kr.order.model.entity.SettlementHistoryEntity;
import co.kr.order.model.vo.SettlementType;
import co.kr.order.repository.SettlementRepository;
import co.kr.order.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    /**
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * todo. 정민님 주석
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    private final SettlementRepository settlementRepository;

    @Override
    @Transactional
    public void createSettlement(Long paymentIdx, Map<Long, BigDecimal> sellerSettlementMap) {

        List<SettlementHistoryEntity> settlementList = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : sellerSettlementMap.entrySet()) {
            settlementList.add(SettlementHistoryEntity.builder()
                    .sellerIdx(entry.getKey())
                    .paymentIdx(paymentIdx)
                    .type(SettlementType.ORDERS_CONFIRMED)
                    .amount(entry.getValue())
                    .build());
        }


        /*
         * 실제 정산 DB 에 저장되었는지
         */
        log.info("========== [정산 로그] DB 저장 (SettlementHistory) ==========");
        log.info("결제번호(PaymentIdx): {}", paymentIdx);
        for (SettlementHistoryEntity entity : settlementList) {
            log.info(" - [저장] 판매자: {}, 타입: {}, 금액: {}",
                    entity.getSellerIdx(), entity.getType(), entity.getAmount());
        }
        log.info("==============================================================");

        settlementRepository.saveAll(settlementList);
    }

    @Override
    @Transactional
    public void refundSettlement(Long orderId, Long paymentIdx) {

        List<SettlementHistoryEntity> settlements = settlementRepository.findAllByPaymentIdx(paymentIdx);

        if (settlements.isEmpty()) {
            log.warn("환불할 정산 데이터를 찾을 수 없습니다! orderId={}, paymentIdx={}", orderId, paymentIdx);
            return;
        }

        for (SettlementHistoryEntity settlement : settlements) {
            // SETTLE_PAYOUT (이미 정산금 지급됨) 상태라면 자동 환불 불가
            if (settlement.getType() == SettlementType.SETTLE_PAYOUT) {
                log.error("========================================");
                log.error("에러: PAYOUT 건에 대한 환불 요청 발생!");
                log.error("확인 필요 - sellerIdx: {}, paymentIdx: {}, amount: {}",
                        settlement.getSellerIdx(), paymentIdx, settlement.getAmount());
                log.error("========================================");
                continue;
            }

            settlement.setType(SettlementType.CANCEL_ADJUST);
            log.info("환불 정산 상태 변경 완료: sellerIdx={}, paymentIdx={}", settlement.getSellerIdx(), paymentIdx);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<SettlementInfo> getSettlementList(Long sellerIdx) {

        List<SettlementHistoryEntity> entities = settlementRepository
        .findAllBySellerIdx(sellerIdx);

        List<SettlementInfo> returnSettlementList = new ArrayList<>();

        for(SettlementHistoryEntity entity : entities) {
            SettlementInfo settlementInfo = new SettlementInfo(
                    entity.getSettlementIdx(),
                    entity.getSellerIdx(),
                    entity.getPaymentIdx(),
                    entity.getType().toString(),
                    entity.getAmount(),
                    entity.getCreatedAt()
            );

            returnSettlementList.add(settlementInfo);
        }

        return returnSettlementList;
    }

    @Transactional(readOnly = true)
    @Override
    public SettlementInfo getSettlement(Long sellerIdx, Long paymentIdx) {

        SettlementHistoryEntity entity = settlementRepository
                .findBySellerIdxAndPaymentIdx(sellerIdx, paymentIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTLEMENT_NOT_FOUND,
                        "정산 정보를 찾을 수 없습니다. sellerIdx=" + sellerIdx + ", paymentIdx=" + paymentIdx));

        return new SettlementInfo(
                entity.getSettlementIdx(),
                entity.getSellerIdx(),
                entity.getPaymentIdx(),
                entity.getType().toString(),
                entity.getAmount(),
                entity.getCreatedAt()
        );
    }
}
