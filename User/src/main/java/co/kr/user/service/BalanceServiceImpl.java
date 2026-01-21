package co.kr.user.service;

import co.kr.user.model.DTO.Payment.PaymentDateOptionReq;
import co.kr.user.model.DTO.Payment.PaymentListDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 사용자 잔액(Balance) 및 결제 내역 관리 비즈니스 로직을 정의하는 인터페이스입니다.
 * 현재 잔액 조회, 전체 거래 내역 조회, 조건별(기간) 거래 내역 조회 기능을 명세합니다.
 * 구현체: BalanceService
 */
public interface BalanceServiceImpl {

    /**
     * 사용자 잔액 조회 메서드 정의입니다.
     * 사용자가 현재 보유하고 있는 예치금(포인트) 잔액을 조회합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @return 현재 잔액 (BigDecimal)
     */
    BigDecimal balance(Long userIdx);

    /**
     * 전체 거래 내역 조회 메서드 정의입니다.
     * 사용자의 모든 예치금 충전 및 사용 이력을 최신순으로 조회합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @return 거래 내역 목록 (PaymentListDTO 리스트)
     */
    List<PaymentListDTO> balanceHistory(Long userIdx);

    /**
     * 조건별 거래 내역 조회 메서드 정의입니다.
     * 특정 기간(시작일, 종료일) 등 지정된 조건에 맞는 예치금 거래 내역을 조회합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param paymentDateOptionReq 조회 조건(날짜 범위 등) 정보
     * @return 조건에 맞는 거래 내역 목록
     */
    List<PaymentListDTO> balanceHistoryOption(Long userIdx, PaymentDateOptionReq paymentDateOptionReq);
}