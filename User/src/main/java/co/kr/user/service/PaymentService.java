package co.kr.user.service;

import co.kr.user.model.DTO.Payment.PaymentReq;
import co.kr.user.model.DTO.Payment.PaymentListDTO;

import java.util.List;

/**
 * 사용자 잔액(Balance) 및 결제 내역 관리 비즈니스 로직을 정의하는 인터페이스입니다.
 * 현재 잔액 조회, 전체 거래 내역 조회, 조건별(기간) 거래 내역 조회 기능을 명세합니다.
 * 구현체: PaymentServiceImpl
 */
public interface PaymentService {

    List<PaymentListDTO> balanceHistory(Long userIdx, PaymentReq paymentReq);
}