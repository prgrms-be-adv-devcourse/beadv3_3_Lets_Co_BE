package co.kr.user.service;

import co.kr.user.model.dto.payment.PaymentReq;
import co.kr.user.model.dto.payment.PaymentListDTO;

import java.util.List;

/**
 * 결제 및 포인트 이용 내역 조회를 위한 서비스 인터페이스입니다.
 * 사용자의 충전, 결제, 환불 등 금전적인 활동 이력을 관리합니다.
 */
public interface PaymentService {

    /**
     * 특정 사용자의 결제/이용 내역(Balance History)을 조회합니다.
     * 날짜 범위, 결제 상태(완료/취소 등), 결제 수단 등을 필터링하여 조회할 수 있습니다.
     * * @param userIdx 사용자 식별자 (PK)
     * @param paymentReq 조회 조건(기간, 상태, 유형)이 담긴 요청 객체
     * @return 조건에 맞는 결제 내역 DTO 리스트
     */
    List<PaymentListDTO> balanceHistory(Long userIdx, PaymentReq paymentReq);
}