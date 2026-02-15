package co.kr.user.model.dto.payment;

import co.kr.user.model.vo.PaymentStatus;
import co.kr.user.model.vo.PaymentType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자의 결제/충전/환불 내역을 특정 조건에 따라 검색(필터링) 요청할 때 사용하는 DTO입니다.
 * 여러 상태나 타입을 동시에 선택하여 조회할 수 있도록 리스트 형태로 설계되어 있습니다.
 */
@Data
public class PaymentReq {
    /** * 조회하고자 하는 결제 상태의 리스트입니다. (예: PAYMENT, CHARGE, REFUND 중 다수 선택)
     * null이거나 비어있을 경우 전체 상태를 조회하는 용도로 활용될 수 있습니다.
     */
    private List<PaymentStatus> paymentStatus;

    /** * 조회하고자 하는 결제 수단 유형의 리스트입니다. (예: CARD, TOSS_PAY 등 다수 선택)
     * 특정 수단으로 결제한 내역만 모아보기 할 때 사용됩니다.
     */
    private List<PaymentType> paymentType;

    /** * 거래 내역 조회의 시작 범위가 되는 일시입니다.
     * 이 시간 이후에 발생한 거래 내역부터 검색 결과에 포함됩니다.
     */
    private LocalDateTime startDate;

    /** * 거래 내역 조회의 종료 범위가 되는 일시입니다.
     * 이 시간 이전에 발생한 거래 내역까지 검색 결과에 포함됩니다.
     */
    private LocalDateTime endDate;
}