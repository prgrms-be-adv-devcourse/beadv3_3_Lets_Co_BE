package co.kr.user.model.dto.client;

import co.kr.user.model.vo.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 시스템 내부 서비스 간의 통신 시 사용자의 잔액(포인트/캐시)을 변경하기 위한 요청 DTO입니다.
 * 결제, 충전, 환불 등의 상황에 따라 잔액을 증감시키는 용도로 사용됩니다.
 */
@Data
public class BalanceReq {
    /** 거래의 성격 및 상태 (PAYMENT: 결제, CHARGE: 충전, REFUND: 환불) */
    private OrderStatus status;
    /** 변경할 금액 (양수값이며, status에 따라 차감 또는 합산 처리됨) */
    private BigDecimal balance;
}