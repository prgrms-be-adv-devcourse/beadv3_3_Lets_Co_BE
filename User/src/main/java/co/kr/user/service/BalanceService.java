package co.kr.user.service;

import co.kr.user.DAO.PaymentRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.model.DTO.Payment.PaymentDateOptionReq;
import co.kr.user.model.DTO.Payment.PaymentListDTO;
import co.kr.user.model.DTO.card.CardListDTO;
import co.kr.user.model.entity.Payment;
import co.kr.user.model.entity.UserCard;
import co.kr.user.model.entity.Users;
import co.kr.user.model.vo.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BalanceService implements BalanceServiceImpl{
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public BigDecimal balance(Long userIdx) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        return users.getBalance();
    }

    @Override
    public List<PaymentListDTO> balanceHistory(Long userIdx) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        List<Payment> paymentList = paymentRepository.findAllByUsersIdxAndTypeOrderByCreatedAtDesc(users.getUsersIdx(), PaymentType.DEPOSIT);

        if (paymentList.isEmpty()) {
            throw new IllegalArgumentException("예치금 결제 내역이 없습니다.");
        }

        return paymentList.stream()
                .map(payment -> {
                    PaymentListDTO dto = new PaymentListDTO();
                    dto.setStatus(payment.getStatus());
                    dto.setAmount(payment.getAmount());
                    dto.setCreatedAt(payment.getCreatedAt());
                    return dto;
                })
                .toList();
    }

    @Override
    public List<PaymentListDTO> balanceHistoryOption(Long userIdx, PaymentDateOptionReq paymentDateOptionReq) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        if (paymentDateOptionReq.getStartDate() == null) {
            paymentDateOptionReq.setStartDate(users.getCreatedAt());
        }

        if (paymentDateOptionReq.getEndDate() == null || paymentDateOptionReq.getEndDate().isAfter(LocalDateTime.now())) {
            paymentDateOptionReq.setEndDate(LocalDateTime.now());
        }

        List<Payment> paymentList = paymentRepository.findAllByUsersIdxAndTypeAndCreatedAtBetweenOrderByCreatedAtDesc(users.getUsersIdx(), PaymentType.DEPOSIT,
                paymentDateOptionReq.getStartDate(), paymentDateOptionReq.getEndDate());

        if (paymentList.isEmpty()) {
            throw new IllegalArgumentException("예치금 결제 내역이 없습니다.");
        }

        return paymentList.stream()
                .map(payment -> {
                    PaymentListDTO dto = new PaymentListDTO();
                    dto.setStatus(payment.getStatus());
                    dto.setAmount(payment.getAmount());
                    dto.setCreatedAt(payment.getCreatedAt());
                    return dto;
                })
                .toList();
    }
}