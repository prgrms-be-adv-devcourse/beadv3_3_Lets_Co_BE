package co.kr.user.service.Impl;

import co.kr.user.dao.PaymentRepository;
import co.kr.user.model.dto.payment.PaymentReq;
import co.kr.user.model.dto.payment.PaymentListDTO;
import co.kr.user.model.entity.Payment;
import co.kr.user.model.entity.Users;
import co.kr.user.model.vo.PaymentStatus;
import co.kr.user.model.vo.PaymentType;
import co.kr.user.service.PaymentService;
import co.kr.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * PaymentService 인터페이스의 구현체입니다.
 * 결제 및 충전, 환불 등의 내역 조회 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회 중심의 서비스이므로 읽기 전용 트랜잭션 기본 적용
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserQueryService userQueryService;

    /**
     * 사용자의 결제/충전 내역(Balance History)을 조회합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @param paymentReq 조회 필터 조건 (상태, 유형, 조회 기간)
     * @return 조회된 내역 목록 DTO 리스트
     */
    @Override
    public List<PaymentListDTO> balanceHistory(Long userIdx, PaymentReq paymentReq) {
        // 사용자 활성 상태 확인
        Users user = userQueryService.findActiveUser(userIdx);

        LocalDateTime now = LocalDateTime.now();

        // 결제 상태 필터가 없으면 모든 상태(완료, 취소 등)를 조회 대상으로 설정
        List<PaymentStatus> statuses = (paymentReq.getPaymentStatus() == null || paymentReq.getPaymentStatus().isEmpty())
                ? Arrays.asList(PaymentStatus.values()) : paymentReq.getPaymentStatus();

        // 결제 유형 필터가 없으면 모든 유형(충전, 결제, 환불 등)을 조회 대상으로 설정
        List<PaymentType> types = (paymentReq.getPaymentType() == null || paymentReq.getPaymentType().isEmpty())
                ? Arrays.asList(PaymentType.values()) : paymentReq.getPaymentType();

        // 조회 시작 날짜 설정: 입력이 없으면 기본적으로 최근 1개월 조회
        LocalDateTime start = paymentReq.getStartDate();
        if (start == null) {
            LocalDateTime oneMonthAgo = now.minusMonths(1);
            // 단, 1개월 전이 가입일보다 이전이면 가입일부터 조회
            start = oneMonthAgo.isBefore(user.getCreatedAt()) ? user.getCreatedAt() : oneMonthAgo;
        }

        // 조회 종료 날짜 설정: 입력이 없거나 미래 날짜이면 현재 시간으로 설정
        LocalDateTime end = (paymentReq.getEndDate() == null || paymentReq.getEndDate().isAfter(now))
                ? now : paymentReq.getEndDate();

        // 조건에 맞는 결제 내역을 최신순으로 조회
        List<Payment> paymentList = paymentRepository.findAllByUsersIdxAndStatusInAndTypeInAndCreatedAtBetweenOrderByCreatedAtDesc(
                user.getUsersIdx(),
                statuses,
                types,
                start,
                end
        );

        // 내역이 없으면 예외 발생 (또는 빈 리스트 반환이 더 나을 수 있음 - 기획에 따라 다름)
        if (paymentList.isEmpty()) {
            throw new IllegalArgumentException("결제 내역이 없습니다.");
        }

        // 엔티티를 DTO로 변환하여 반환
        return paymentList.stream()
                .map(payment -> {
                    PaymentListDTO dto = new PaymentListDTO();
                    dto.setStatus(payment.getStatus());
                    dto.setType(payment.getType());
                    dto.setAmount(payment.getAmount());
                    dto.setCreatedAt(payment.getCreatedAt());
                    return dto;
                })
                .toList();
    }
}