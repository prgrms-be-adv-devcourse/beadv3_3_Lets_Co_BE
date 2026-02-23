package co.kr.order.model.dto.request;

/*
 * 환불 요청 정보
 * @param userIdx: 유저 인덱스
 * @param orderCode: 주문 코드
 */
public record ClientRefundReq(
        Long userIdx,
        String orderCode
) {}
