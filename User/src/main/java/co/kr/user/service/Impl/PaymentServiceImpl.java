package co.kr.user.service.Impl;

import co.kr.user.dao.PaymentRepository;
import co.kr.user.model.dto.payment.PaymentReq;
import co.kr.user.model.dto.payment.PaymentListDTO;
import co.kr.user.model.entity.Payment;
import co.kr.user.model.entity.Users;
import co.kr.user.model.vo.PaymentStatus;
import co.kr.user.model.vo.PaymentType;
import co.kr.user.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;

    private final UserQueryServiceImpl userQueryServiceImpl;

    @Override
    public List<PaymentListDTO> balanceHistory(Long userIdx, PaymentReq paymentReq) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        LocalDateTime now = LocalDateTime.now();

        List<PaymentStatus> statuses = (paymentReq.getPaymentStatus() == null || paymentReq.getPaymentStatus().isEmpty())
                ? Arrays.asList(PaymentStatus.values()) : paymentReq.getPaymentStatus();

        List<PaymentType> types = (paymentReq.getPaymentType() == null || paymentReq.getPaymentType().isEmpty())
                ? Arrays.asList(PaymentType.values()) : paymentReq.getPaymentType();

        LocalDateTime start = paymentReq.getStartDate();
        if (start == null) {
            LocalDateTime oneMonthAgo = now.minusMonths(1);
            start = oneMonthAgo.isBefore(users.getCreatedAt()) ? users.getCreatedAt() : oneMonthAgo;
        }

        LocalDateTime end = (paymentReq.getEndDate() == null || paymentReq.getEndDate().isAfter(now))
                ? now : paymentReq.getEndDate();

        List<Payment> paymentList = paymentRepository.findAllByUsersIdxAndStatusInAndTypeInAndCreatedAtBetweenOrderByCreatedAtDesc(
                users.getUsersIdx(),
                statuses,
                types,
                start,
                end
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