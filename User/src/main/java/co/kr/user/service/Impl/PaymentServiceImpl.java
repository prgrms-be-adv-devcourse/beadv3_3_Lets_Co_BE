package co.kr.user.service.Impl;

import co.kr.user.dao.PaymentRepository;
import co.kr.user.dao.UserRepository;
import co.kr.user.model.dto.Payment.PaymentReq;
import co.kr.user.model.dto.Payment.PaymentListDTO;
import co.kr.user.model.entity.Payment;
import co.kr.user.model.entity.Users;
import co.kr.user.model.vo.PaymentStatus;
import co.kr.user.model.vo.PaymentType;
import co.kr.user.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 사용자의 예치금(포인트) 잔액 및 결제/충전 내역을 관리하는 서비스 클래스입니다.
 * 현재 잔액 조회, 전체 충전 내역 조회, 기간별 내역 조회 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    private final UserQueryServiceImpl userQueryServiceImpl;

    @Override
    public List<PaymentListDTO> balanceHistory(Long userIdx, PaymentReq paymentReq) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        // List가 비어있으면 전체 조회로 간주하여 모든 Enum 값 주입
        if (paymentReq.getPaymentType() == null || paymentReq.getPaymentType().isEmpty()) {
            paymentReq.setPaymentType(Arrays.asList(PaymentType.values()));
        }

        if (paymentReq.getPaymentStatus() == null || paymentReq.getPaymentStatus().isEmpty()) {
            paymentReq.setPaymentStatus(Arrays.asList(PaymentStatus.values()));
        }

        if (paymentReq.getStartDate() == null) {
            paymentReq.setStartDate(users.getCreatedAt());
        }
        if (paymentReq.getEndDate() == null || paymentReq.getEndDate().isAfter(LocalDateTime.now())) {
            paymentReq.setEndDate(LocalDateTime.now());
        }

        // [수정] Repository 메서드 변경에 맞춰 호출 (StatusIn, TypeIn)
        List<Payment> paymentList = paymentRepository.findAllByUsersIdxAndStatusInAndTypeInAndCreatedAtBetweenOrderByCreatedAtDesc(
                users.getUsersIdx(),
                paymentReq.getPaymentStatus(), // List<PaymentStatus>
                paymentReq.getPaymentType(),   // List<PaymentType>
                paymentReq.getStartDate(),
                paymentReq.getEndDate()
        );

        if (paymentList.isEmpty()) {
            throw new IllegalArgumentException("결제 내역이 없습니다.");
        }

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