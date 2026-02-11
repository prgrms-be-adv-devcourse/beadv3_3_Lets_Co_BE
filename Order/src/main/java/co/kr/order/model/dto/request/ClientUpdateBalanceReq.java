package co.kr.order.model.dto.request;

import java.math.BigDecimal;

/*
 * 예치금 수정 요청 정보
 * @param userIdx: 유저 인덱스
 * @param amount: 금액
 */
public record ClientUpdateBalanceReq(
        Long userIdx,
        BigDecimal amount
) {}
